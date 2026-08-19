# PLAN — Fix n8n Order Tracking Wiring & Error Routing

## 1. Mục tiêu

Sửa các connection trong workflow n8n:

```text
n8n/Telegram Chatbot - Chat Bot TheXuong Final.json
```

Mục tiêu:

- Sửa các connection đang đi vào `input index = 1` không cần thiết.
- Đảm bảo HTTP Request lỗi vẫn đi qua formatter/fallback thay vì dừng flow.
- Đảm bảo `Load Memory` lỗi vẫn có thể tiếp tục sang `Parse Order Tracking`.
- Không sửa business logic hiện tại nếu không cần.
- Không thay đổi prompt Ollama.
- Không thay đổi logic detect intent.
- Không thay đổi backend trong plan này.

---

# 2. Scope bắt buộc

Chỉ được phép sửa:

```text
connections
error routing
node error output wiring
```

Các node liên quan:

```text
Load Memory
Parse Order Tracking
Order Routing
Kiểm Tra Fetch Đơn
Lấy Dữ Liệu Đơn Hàng
Format Order Response
Xử Lý Order Reply
Chuẩn Bị Save Memory
Gửi Phản Hồi Telegram
```

Không rewrite toàn workflow.

Không đổi tên node.

Không xóa node.

Không thêm dependency.

Không sửa code bên trong node trừ khi wiring hiện tại bắt buộc phải chỉnh một dòng nhỏ để tương thích.

---

# 3. Trạng thái hiện tại cần sửa

## ISSUE 1 — Format Order Response đang nối vào input index 1

Hiện tại:

```text
Format Order Response
    ↓
Xử Lý Order Reply
input index = 1
```

Yêu cầu sửa thành:

```text
Format Order Response
    ↓
Xử Lý Order Reply
input index = 0
```

### Expected connection

```json
{
  "node": "Xử Lý Order Reply",
  "type": "main",
  "index": 0
}
```

---

## ISSUE 2 — Xử Lý Order Reply đang nối vào Chuẩn Bị Save Memory bằng index 1

Hiện tại:

```text
Xử Lý Order Reply
    ↓
Chuẩn Bị Save Memory
input index = 1
```

Yêu cầu sửa thành:

```text
Xử Lý Order Reply
    ↓
Chuẩn Bị Save Memory
input index = 0
```

### Expected connection

```json
{
  "node": "Chuẩn Bị Save Memory",
  "type": "main",
  "index": 0
}
```

---

## ISSUE 3 — Xử Lý Order Reply đang nối vào Gửi Phản Hồi Telegram bằng index 1

Hiện tại:

```text
Xử Lý Order Reply
    ↓
Gửi Phản Hồi Telegram
input index = 1
```

Yêu cầu sửa thành:

```text
Xử Lý Order Reply
    ↓
Gửi Phản Hồi Telegram
input index = 0
```

### Expected connection

```json
{
  "node": "Gửi Phản Hồi Telegram",
  "type": "main",
  "index": 0
}
```

---

# 4. Error routing bắt buộc bổ sung

## ISSUE 4 — Error output của Lấy Dữ Liệu Đơn Hàng chưa được xử lý

Node:

```text
Lấy Dữ Liệu Đơn Hàng
```

đang dùng:

```text
onError = continueErrorOutput
```

nhưng hiện chỉ có success path:

```text
Lấy Dữ Liệu Đơn Hàng
    ↓
Format Order Response
```

Yêu cầu:

```text
Lấy Dữ Liệu Đơn Hàng
    ├── Success → Format Order Response
    └── Error   → Format Order Response
```

Cả hai đều phải vào:

```text
Format Order Response
input index = 0
```

Mục tiêu:

```text
HTTP success → formatter xử lý success/not-found
HTTP error   → formatter trả fallback lỗi hệ thống
```

Không để HTTP error kết thúc execution tại node này.

---

## ISSUE 5 — Error output của Load Memory chưa được xử lý

Node:

```text
Load Memory
```

đang dùng:

```text
onError = continueErrorOutput
```

Yêu cầu:

```text
Load Memory
    ├── Success → Parse Order Tracking
    └── Error   → Parse Order Tracking
```

Cả hai đều vào:

```text
Parse Order Tracking
input index = 0
```

Mục tiêu:

- API memory lỗi không làm toàn workflow chết.
- `Parse Order Tracking` vẫn chạy với fallback state rỗng.
- Message thông thường vẫn tiếp tục xử lý.

---

# 5. Target flow sau khi fix

Workflow phải có routing:

```text
Smart Filter + Intent
        ↓
Load Memory
   ┌────┴────┐
Success    Error
   └────┬────┘
        ↓
Parse Order Tracking
        ↓
Order Routing
   ┌────┴─────────────┐
 TRUE                FALSE
   ↓                   ↓
Kiểm Tra Fetch Đơn  Build Prompt
 ┌────┴────┐            ↓
TRUE      FALSE        Ollama AI
 ↓          ↓            ↓
Order API  Order Reply Post-Validation
 │          │            ↓
 ├ Success  │       Xử Lý Phản Hồi AI
 └ Error    │            │
     ↓      │            │
Format Order Response   │
     ↓      │            │
     └────→ Xử Lý Order Reply
               │
               ├──→ Chuẩn Bị Save Memory
               │       ├──→ Save Memory
               │       └──→ Logging
               │
               └──→ Gửi Phản Hồi Telegram
```

Nhánh AI giữ nguyên:

```text
Build Prompt
→ Ollama AI
→ Post-Validation
→ Xử Lý Phản Hồi AI
→ Chuẩn Bị Save Memory
→ Gửi Phản Hồi Telegram
```

---

# 6. Task Breakdown

## TASK 1 — Backup và audit connection hiện tại

### Thực hiện

Trước khi sửa:

1. Backup workflow JSON hiện tại.
2. Kiểm tra object:

```text
connections
```

3. Ghi nhận connection hiện tại của:

```text
Load Memory
Lấy Dữ Liệu Đơn Hàng
Format Order Response
Xử Lý Order Reply
```

### Không sửa code ở task này.

### Done khi

Có báo cáo:

```text
TASK 1 REPORT

Backup:
DONE / FAIL

Current connections:
- Format Order Response -> Xử Lý Order Reply : index ?
- Xử Lý Order Reply -> Chuẩn Bị Save Memory : index ?
- Xử Lý Order Reply -> Gửi Phản Hồi Telegram : index ?
- Order API error output connected: YES / NO
- Load Memory error output connected: YES / NO
```

---

## TASK 2 — Fix 3 input index

Sửa:

```text
Format Order Response
→ Xử Lý Order Reply
index 1 → 0
```

Sửa:

```text
Xử Lý Order Reply
→ Chuẩn Bị Save Memory
index 1 → 0
```

Sửa:

```text
Xử Lý Order Reply
→ Gửi Phản Hồi Telegram
index 1 → 0
```

### Không thay connection khác.

### Validation

Sau khi sửa phải xác nhận JSON có:

```text
Format Order Response
→ Xử Lý Order Reply
index = 0
```

```text
Xử Lý Order Reply
→ Chuẩn Bị Save Memory
index = 0
```

```text
Xử Lý Order Reply
→ Gửi Phản Hồi Telegram
index = 0
```

### Progress report

```text
TASK 2 REPORT

Changed connections:
1. ...
2. ...
3. ...

JSON validation:
PASS / FAIL

Unexpected changes:
NONE / ...
```

---

## TASK 3 — Connect Order API error output

Node:

```text
Lấy Dữ Liệu Đơn Hàng
```

Phải có:

```text
Success → Format Order Response
Error   → Format Order Response
```

### Yêu cầu

Không tạo formatter mới.

Không gửi error qua Ollama.

Không nối error trực tiếp Telegram.

Tất cả order API response phải đi qua:

```text
Format Order Response
```

### Expected result

Nếu API trả lỗi/network timeout:

```text
Format Order Response
```

phải có cơ hội tạo reply:

```text
Hệ thống tra cứu đơn hàng đang tạm thời gặp sự cố. Bạn vui lòng thử lại sau nhé.
```

### Progress report

```text
TASK 3 REPORT

Order API success path:
PASS / FAIL

Order API error path:
PASS / FAIL

Error destination:
Format Order Response

Ollama bypass:
YES / NO
```

---

## TASK 4 — Connect Load Memory error output

Node:

```text
Load Memory
```

Phải có:

```text
Success → Parse Order Tracking
Error   → Parse Order Tracking
```

### Yêu cầu

Không bypass:

```text
Parse Order Tracking
```

Không nối error trực tiếp:

```text
Build Prompt
```

hoặc:

```text
Telegram
```

### Expected

Nếu memory API lỗi:

```text
Parse Order Tracking
```

vẫn chạy.

Nếu không có state:

```text
state = {}
```

và flow tiếp tục bình thường.

### Progress report

```text
TASK 4 REPORT

Load Memory success path:
PASS / FAIL

Load Memory error path:
PASS / FAIL

Parse Order Tracking still executes on memory error:
PASS / FAIL
```

---

# 7. Structural Validation

Sau khi sửa xong phải kiểm tra toàn bộ connection graph.

## Order routing

Phải đúng:

```text
Parse Order Tracking
→ Order Routing
```

```text
Order Routing TRUE
→ Kiểm Tra Fetch Đơn
```

```text
Order Routing FALSE
→ Build Prompt
```

---

## Fetch routing

Phải đúng:

```text
Kiểm Tra Fetch Đơn TRUE
→ Lấy Dữ Liệu Đơn Hàng
```

```text
Kiểm Tra Fetch Đơn FALSE
→ Xử Lý Order Reply
```

---

## API routing

Phải đúng:

```text
Lấy Dữ Liệu Đơn Hàng SUCCESS
→ Format Order Response
```

```text
Lấy Dữ Liệu Đơn Hàng ERROR
→ Format Order Response
```

---

## Formatter routing

Phải đúng:

```text
Format Order Response
→ Xử Lý Order Reply
index 0
```

---

## Reply routing

Phải đúng:

```text
Xử Lý Order Reply
→ Chuẩn Bị Save Memory
index 0
```

và:

```text
Xử Lý Order Reply
→ Gửi Phản Hồi Telegram
index 0
```

---

# 8. Test Matrix bắt buộc

## TEST 1 — Normal AI chat

Input:

```text
xin chào
```

Expected:

```text
Smart Filter
→ Load Memory
→ Parse Order Tracking
→ Order Routing FALSE
→ Build Prompt
→ Ollama
→ Telegram
```

PASS khi:

- không đi order API
- Telegram có reply

---

## TEST 2 — Order lookup bắt đầu

Input:

```text
tra cứu đơn hàng
```

Expected:

```text
Order Routing TRUE
Kiểm Tra Fetch Đơn FALSE
Xử Lý Order Reply
```

Bot hỏi phone.

PASS khi:

- không gọi Ollama
- không gọi Order API
- state được save
- Telegram có reply

---

## TEST 3 — Có đủ phone + orderId

Input ví dụ:

```text
tra cứu đơn 125 số điện thoại 0912345678
```

Expected:

```text
Order Routing TRUE
Kiểm Tra Fetch Đơn TRUE
Order API
Format Order Response
Xử Lý Order Reply
Telegram
```

PASS khi:

- không qua Ollama
- Telegram trả order response

---

## TEST 4 — Order API trả not-found

Expected:

```text
Order API
→ Format Order Response
→ Xử Lý Order Reply
→ Telegram
```

PASS khi bot trả message không tìm thấy đơn.

---

## TEST 5 — Order API network/error output

Giả lập lỗi HTTP hoặc endpoint lỗi.

Expected:

```text
Order API ERROR
→ Format Order Response
→ Xử Lý Order Reply
→ Telegram
```

PASS khi bot trả:

```text
Hệ thống tra cứu đơn hàng đang tạm thời gặp sự cố...
```

Workflow không được dừng tại Order API.

---

## TEST 6 — Load Memory API error

Giả lập memory endpoint lỗi.

Expected:

```text
Load Memory ERROR
→ Parse Order Tracking
→ tiếp tục workflow
```

PASS khi workflow không dừng tại `Load Memory`.

---

## TEST 7 — Missing field multi-message

Conversation:

```text
Khách: tra cứu đơn
Bot: hỏi phone

Khách: 0912345678
Bot: hỏi mã đơn

Khách: 125
Bot: trả order
```

PASS khi cả 3 message chạy đúng.

---

# 9. Regression Checklist

Sau khi sửa:

- [ ] Telegram Trigger hoạt động.
- [ ] FAQ flow hoạt động.
- [ ] Product flow hoạt động.
- [ ] Greeting hoạt động.
- [ ] Ollama flow hoạt động.
- [ ] Order flow không qua Ollama.
- [ ] Order success hoạt động.
- [ ] Order not-found hoạt động.
- [ ] Order HTTP error có fallback.
- [ ] Load Memory error không làm chết workflow.
- [ ] Save Memory vẫn chạy.
- [ ] Logging vẫn chạy.
- [ ] Telegram reply vẫn gửi.
- [ ] Workflow JSON parse thành công.
- [ ] Không có dangling connection mới.
- [ ] Không có node bị duplicate ngoài ý muốn.

---

# 10. Quy tắc cho Coding Agent

Bắt buộc:

1. Chỉ sửa wiring/error routing được mô tả trong plan.
2. Không refactor code khác.
3. Không rewrite `Smart Filter + Intent`.
4. Không rewrite `Parse Order Tracking`.
5. Không rewrite `Build Prompt`.
6. Không sửa backend.
7. Không thay credential.
8. Không hardcode secret mới.
9. Không xóa node.
10. Không đổi tên node.
11. Không thay đổi node position trừ khi cần để dễ nhìn.
12. Nếu n8n UI tự thay đổi metadata/version ID thì báo rõ trong report.
13. Sau mỗi task phải validate JSON.
14. Nếu phát hiện cần sửa logic ngoài scope, STOP và báo BLOCKED trước khi sửa.

---

# 11. Progress Tracking

| Task | Nội dung | Status | Test |
|---|---|---|---|
| 1 | Backup + audit connections | DONE | PASS (backup: workflow-backup-before-wiring-fix-20260818.json) |
| 2 | Fix 3 input indexes | DONE | PASS (JSON validation) |
| 3 | Order API error routing | DONE | PASS (error -> Format Order Response, bypass Ollama) |
| 4 | Load Memory error routing | DONE | PASS (error -> Parse Order Tracking, state={} fallback) |
| 5 | Structural validation | DONE | PASS (17/17 connection, no dangling) |
| 6 | Full test matrix | DONE | PASS (39/39 fix-cases + 26/26 sim-cases regression = 65/65) |

Status chỉ dùng:

```text
TODO
IN_PROGRESS
BLOCKED
DONE
```

---

# 12. Format báo cáo sau mỗi task

```text
## TASK X REPORT

Status:
DONE / BLOCKED

File changed:
n8n/Telegram Chatbot - Chat Bot TheXuong Final.json

Connections changed:
- source -> target : old index -> new index
- ...

Tests:
- ...

Result:
PASS / FAIL

Unexpected changes:
NONE / ...

Next:
TASK X+1
```

Nếu BLOCKED:

```text
Blocker:
...

Evidence:
...

Required decision:
...
```

---

# 13. Final Connection Report bắt buộc

Sau khi hoàn tất agent phải xuất chính xác bảng này:

| Source | Output | Target | Target Input | Status |
|---|---|---|---|---|
| Load Memory | Success | Parse Order Tracking | 0 | PASS/FAIL |
| Load Memory | Error | Parse Order Tracking | 0 | PASS/FAIL |
| Parse Order Tracking | Main | Order Routing | 0 | PASS/FAIL |
| Order Routing | True | Kiểm Tra Fetch Đơn | 0 | PASS/FAIL |
| Order Routing | False | Build Prompt | 0 | PASS/FAIL |
| Kiểm Tra Fetch Đơn | True | Lấy Dữ Liệu Đơn Hàng | 0 | PASS/FAIL |
| Kiểm Tra Fetch Đơn | False | Xử Lý Order Reply | 0 | PASS/FAIL |
| Lấy Dữ Liệu Đơn Hàng | Success | Format Order Response | 0 | PASS/FAIL |
| Lấy Dữ Liệu Đơn Hàng | Error | Format Order Response | 0 | PASS/FAIL |
| Format Order Response | Main | Xử Lý Order Reply | 0 | PASS/FAIL |
| Xử Lý Order Reply | Main | Chuẩn Bị Save Memory | 0 | PASS/FAIL |
| Xử Lý Order Reply | Main | Gửi Phản Hồi Telegram | 0 | PASS/FAIL |

---

# 14. Definition of Done

Chỉ DONE khi:

- [ ] 3 connection index `1` đã đổi về `0`.
- [ ] Order API error output đi tới `Format Order Response`.
- [ ] Load Memory error output đi tới `Parse Order Tracking`.
- [ ] Order flow success hoạt động.
- [ ] Order flow missing-field hoạt động.
- [ ] Order API failure có fallback Telegram.
- [ ] Memory API failure không làm workflow chết.
- [ ] Normal Ollama flow vẫn hoạt động.
- [ ] Save Memory vẫn chạy.
- [ ] Logging vẫn chạy.
- [ ] Telegram reply vẫn hoạt động.
- [ ] JSON workflow hợp lệ.
- [ ] Không phát sinh thay đổi ngoài scope.

---

# 15. Kết quả thực hiện

## Final Connection Report (sau khi fix — validated bằng script trên workflow JSON thật)

| Source | Output | Target | Target Input | Status |
|---|---|---|---|---|
| Load Memory | Success | Parse Order Tracking | 0 | PASS |
| Load Memory | Error | Parse Order Tracking | 0 | PASS |
| Parse Order Tracking | Main | Order Routing | 0 | PASS |
| Order Routing | True | Kiểm Tra Fetch Đơn | 0 | PASS |
| Order Routing | False | Build Prompt | 0 | PASS |
| Kiểm Tra Fetch Đơn | True | Lấy Dữ Liệu Đơn Hàng | 0 | PASS |
| Kiểm Tra Fetch Đơn | False | Xử Lý Order Reply | 0 | PASS |
| Lấy Dữ Liệu Đơn Hàng | Success | Format Order Response | 0 | PASS |
| Lấy Dữ Liệu Đơn Hàng | Error | Format Order Response | 0 | PASS |
| Format Order Response | Main | Xử Lý Order Reply | 0 | PASS |
| Xử Lý Order Reply | Main | Chuẩn Bị Save Memory | 0 | PASS |
| Xử Lý Order Reply | Main | Gửi Phản Hồi Telegram | 0 | PASS |

## Test results

- `fix-cases.js` (TEST 1-7 + regression của plan này): **39 PASS / 0 FAIL**
- `sim-cases.js` (regression plan trước, 10 case + 3 bonus): **26 PASS / 0 FAIL**
- Structural validation: 17/17 connection PASS, không có dangling connection, 22 nodes không đổi.
- Không thay đổi ngoài scope: không đổi tên/xóa/thêm node, không sửa backend, không sửa logic node (chỉ cập nhật 1 comment mô tả trong `Xử Lý Order Reply` sau khi đã được duyệt).

## Files changed

- `n8n/Telegram Chatbot - Chat Bot TheXuong Final.json` (wiring: 3 input index 1→0, +2 error connections)
- `n8n/workflow-backup-before-wiring-fix-20260818.json` (backup mới)
- Simulator (temp, không thuộc repo): `sim-core.js` (error simulation + error routing), `fix-cases.js` (test matrix mới)
