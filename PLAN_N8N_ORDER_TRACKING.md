# PLAN — Cải thiện n8n Order Tracking Chatbot

## 1. Mục tiêu

Cải thiện workflow Telegram Chatbot của TheXuong để chức năng **tra cứu đơn hàng** hoạt động theo hội thoại nhiều bước.

Hiện tại workflow chỉ tra cứu khi khách gửi đủ:

- intent `order_tracking`
- số điện thoại
- mã đơn hàng

trong cùng một message.

Mục tiêu mới:

```text
Khách: Tra cứu đơn hàng
Bot: Vui lòng cung cấp số điện thoại.

Khách: 0912345678
Bot: Vui lòng cung cấp mã đơn hàng.

Khách: 125
Bot: Trả về thông tin đơn hàng #125.
```

Workflow phải nhớ trạng thái tra cứu theo `chatId`, chấp nhận dữ liệu được gửi ở nhiều message khác nhau và không phụ thuộc vào Ollama để xử lý business logic tra cứu đơn.

---

# 2. Phạm vi thực hiện

Các phần chính cần sửa:

```text
n8n/
└── Telegram Chatbot - Chat Bot TheXuong Final.json

src/main/java/com/example/thexuong/
├── controller/ChatbotController.java
├── dto/ChatMemoryRequest.java
├── entity/ChatMemory.java
├── service/ChatbotService.java
├── repository/ChatMemoryRepository.java
└── security / config liên quan chatbot API nếu cần
```

Có thể tạo thêm DTO/helper nếu giúp code rõ hơn.

Không thay đổi các chức năng chatbot sản phẩm, FAQ hoặc các chức năng thương mại điện tử khác ngoài phần cần thiết để hỗ trợ state của chatbot.

---

# 3. Kiến trúc mong muốn

```text
Telegram Trigger
      ↓
Parse Message
      ↓
Load Chat Memory / State
      ↓
Detect Intent + Extract phone/orderId
      ↓
Merge với Order Tracking State cũ
      ↓
     order_tracking?
      ↓
 ┌───────────────┐
 │ đủ phone + id │
 └───────┬───────┘
         │
   YES   │   NO
    ↓    │    ↓
Order API│ Save pending state
    ↓    │    ↓
Validate │ Ask missing field
    ↓
Format Response
    ↓
Clear order state
    ↓
Telegram
```

Business logic tra cứu đơn phải deterministic.

Không dùng Ollama để quyết định kết quả đơn hàng.

---

# 4. State cần lưu

Bổ sung state có cấu trúc cho mỗi `chatId`.

Ví dụ:

```json
{
  "orderLookup": {
    "active": true,
    "phone": "0912345678",
    "orderId": null,
    "startedAt": "2026-08-18T18:20:00"
  }
}
```

Khuyến nghị lưu bằng field:

```text
state_json
```

trong bảng `chat_memory`.

Không nhét state vào `history_json`.

---

# 5. Task Breakdown

## TASK 1 — Audit workflow hiện tại

### Việc cần làm

Đọc toàn bộ workflow:

```text
n8n/Telegram Chatbot - Chat Bot TheXuong Final.json
```

Xác định chính xác các node liên quan:

```text
Telegram Trigger
Kiểm Tra Loại Message
Lấy Dữ Liệu FAQ
Lấy Dữ Liệu Sản Phẩm
Smart Filter + Intent
Kiểm Tra Tra Cứu
Lấy Dữ Liệu Đơn Hàng
Load Memory
Build Prompt
Ollama AI
Xử Lý Phản Hồi AI
Chuẩn Bị Save Memory
Save Memory
Logging
Gửi Phản Hồi Telegram
```

Xác nhận flow hiện tại của `order_tracking`.

### Không sửa code trong task này.

### Done khi

Có báo cáo ngắn:

```text
TASK 1 REPORT

Current order flow:
...

Current limitation:
...

Nodes affected:
...
```

---

## TASK 2 — Bổ sung chatbot state vào backend

### Entity

Sửa:

```text
ChatMemory.java
```

Thêm:

```java
@Column(name = "state_json", columnDefinition = "NVARCHAR(MAX)")
private String stateJson;
```

Default logic phải đảm bảo state không null khi đọc.

Ví dụ default:

```json
{}
```

### DTO

Sửa:

```text
ChatMemoryRequest.java
```

Thêm:

```java
private String stateJson;
```

### Service

Sửa:

```text
ChatbotService.java
```

`getChatMemory(chatId)` phải trả được cả:

```json
{
  "historyJson": "...",
  "stateJson": "..."
}
```

Nếu chưa có memory:

```json
historyJson = []
stateJson = {}
```

`saveChatMemory(...)` phải save cả:

```text
historyJson
stateJson
```

Không được làm mất history hiện tại khi chỉ update state.

### Controller

Sửa:

```text
ChatbotController.java
```

GET:

```text
/api/v1/chatbot/memory/{chatId}
```

Response mong muốn:

```json
{
  "success": true,
  "chatId": "123456789",
  "history_json": "[]",
  "state_json": "{}"
}
```

POST:

```text
/api/v1/chatbot/memory
```

nhận:

```json
{
  "chatId": "123456789",
  "historyJson": "[]",
  "stateJson": "{}"
}
```

### Done khi

Backend compile thành công.

GET/POST memory hoạt động.

Không làm hỏng lịch sử chat cũ.

### Progress report

```text
TASK 2 REPORT

Files changed:
- ...

Database change:
- ...

API response before:
...

API response after:
...

Build result:
PASS / FAIL

Notes:
...
```

---

## TASK 3 — Parse order tracking state trong n8n

Sửa node:

```text
Smart Filter + Intent
```

hoặc tách thành node mới nếu cần:

```text
Parse Order Tracking
```

### Input cần đọc

Từ message hiện tại:

```text
userMessage
chatId
```

Từ memory:

```text
state_json
```

### Extract

Tìm:

```text
phone
orderId
```

Phone phải hỗ trợ:

```text
0912345678
091 234 5678
091-234-5678
091.234.5678
```

Normalize thành 10 chữ số.

### Merge state

Nếu:

```json
state.orderLookup.active = true
```

thì message tiếp theo phải được coi là continuation của `order_tracking`, kể cả message không chứa từ:

```text
đơn hàng
tra cứu
kiểm tra đơn
```

Ví dụ:

```text
Bot: Gửi mã đơn giúp mình.
Khách: 125
```

phải được hiểu:

```json
{
  "intent": "order_tracking",
  "orderId": "125"
}
```

### Output mong muốn

```json
{
  "intent": "order_tracking",
  "orderPhone": "0912345678",
  "orderId": "125",
  "orderLookupActive": true,
  "missingOrderPhone": false,
  "missingOrderId": false,
  "needOrderFetch": true
}
```

### Done khi

Đúng ít nhất các case:

```text
tra cứu đơn hàng
tra cứu đơn 125
tra cứu 0912345678
tra cứu đơn 125 0912345678
0912345678
125
```

khi có pending state phù hợp.

### Progress report

```text
TASK 3 REPORT

Node changed:
...

State merge logic:
...

Supported inputs:
...

Test cases:
1. ...
2. ...
3. ...

Result:
PASS / FAIL
```

---

## TASK 4 — Tạo deterministic order conversation flow

Không đưa order lookup chưa đủ dữ liệu qua Ollama.

Tạo nhánh riêng.

### Case A — thiếu cả phone và orderId

Input:

```text
Khách: Tra cứu đơn hàng
```

Bot:

```text
📦 Được ạ. Bạn vui lòng cung cấp số điện thoại dùng khi đặt hàng nhé.
```

Save:

```json
{
  "orderLookup": {
    "active": true,
    "phone": null,
    "orderId": null
  }
}
```

---

### Case B — có phone, thiếu orderId

Bot:

```text
✅ Mình đã nhận số điện thoại.

Bạn gửi thêm mã đơn hàng giúp mình nhé.
Ví dụ: 125
```

Save:

```json
{
  "orderLookup": {
    "active": true,
    "phone": "0912345678",
    "orderId": null
  }
}
```

---

### Case C — có orderId, thiếu phone

Bot:

```text
✅ Mình đã nhận mã đơn hàng.

Bạn gửi thêm số điện thoại đã dùng khi đặt hàng giúp mình nhé.
```

Save:

```json
{
  "orderLookup": {
    "active": true,
    "phone": null,
    "orderId": "125"
  }
}
```

---

### Case D — đủ phone + orderId

Set:

```text
needOrderFetch = true
```

Gọi:

```text
GET /api/v1/chatbot/orders/track?id={orderId}&phone={phone}
```

### Done khi

Order flow không cần Ollama để hỏi thông tin còn thiếu.

### Progress report

```text
TASK 4 REPORT

New/changed nodes:
...

Routing:
...

Cases implemented:
A PASS/FAIL
B PASS/FAIL
C PASS/FAIL
D PASS/FAIL
```

---

## TASK 5 — Format order response trực tiếp

Sau node:

```text
Lấy Dữ Liệu Đơn Hàng
```

tạo node Code:

```text
Format Order Response
```

Không gửi kết quả qua:

```text
Build Prompt
Ollama AI
```

### Success response

Format ví dụ:

```text
📦 ĐƠN HÀNG #125

🟡 Trạng thái: Đang xử lý
💰 Tổng tiền: 1.590.000 VNĐ
💳 Thanh toán: COD

🛍 Sản phẩm:
• 1x Nike Air Max (Size 42)
• 2x Adidas T-Shirt (Size L)

Nếu bạn cần hỗ trợ thêm về đơn này cứ nhắn mình nhé.
```

Map status sang tiếng Việt nếu backend trả enum.

Ví dụ:

```text
PENDING -> Chờ xử lý
CONFIRMED -> Đã xác nhận
PROCESSING -> Đang xử lý
SHIPPING -> Đang giao hàng
COMPLETED -> Hoàn thành
CANCELLED -> Đã hủy
```

Chỉ map các status thực sự tồn tại trong project.

Không tự tạo enum mới.

### Không tìm thấy đơn

Bot:

```text
Mình không tìm thấy đơn hàng phù hợp với mã đơn và số điện thoại bạn cung cấp.

Bạn kiểm tra lại thông tin rồi gửi lại giúp mình nhé.
```

### API lỗi

Bot:

```text
Hệ thống tra cứu đơn hàng đang tạm thời gặp sự cố. Bạn vui lòng thử lại sau nhé.
```

Không expose stack trace, URL nội bộ hoặc lỗi backend.

### Done khi

Kết quả order không đi qua Ollama.

### Progress report

```text
TASK 5 REPORT

Formatter node:
...

Success output:
...

Not-found output:
...

Error output:
...

Ollama bypass:
YES / NO
```

---

## TASK 6 — Clear state sau khi tra cứu xong

Nếu order lookup thành công hoặc trả kết quả not-found hợp lệ:

clear:

```json
{
  "orderLookup": {
    "active": false,
    "phone": null,
    "orderId": null
  }
}
```

Không để state cũ ảnh hưởng message tiếp theo.

Nếu lỗi hệ thống tạm thời:

có thể giữ state để khách retry.

### Timeout

Nếu `startedAt` quá cũ, tự reset pending order lookup.

Khuyến nghị:

```text
15 phút
```

Không bắt buộc dùng scheduler.

Chỉ cần kiểm tra timestamp khi message mới tới.

### Done khi

State không bị stale sau một cuộc tra cứu hoàn chỉnh.

### Progress report

```text
TASK 6 REPORT

Clear conditions:
...

Timeout:
...

Retry behavior:
...
```

---

## TASK 7 — Đồng bộ Save Memory

Sửa node:

```text
Chuẩn Bị Save Memory
Save Memory
```

Payload mới:

```json
{
  "chatId": "...",
  "historyJson": "...",
  "stateJson": "..."
}
```

Đảm bảo:

- history vẫn lưu tối đa số lượt hiện tại
- order state không bị ghi đè ngoài ý muốn
- chatbot product/FAQ vẫn hoạt động
- state được cập nhật kể cả khi bypass Ollama

### Done khi

State persist qua nhiều Telegram messages.

### Progress report

```text
TASK 7 REPORT

Save payload:
...

Multi-message persistence:
PASS / FAIL

Regression:
PASS / FAIL
```

---

## TASK 8 — Bảo vệ API order tracking

Endpoint hiện tại:

```text
/api/v1/chatbot/orders/track
```

không nên chỉ dựa vào public access.

Triển khai tối thiểu một shared secret giữa n8n và backend.

Khuyến nghị header:

```text
X-Chatbot-Secret
```

Backend đọc từ environment variable.

Ví dụ:

```text
CHATBOT_API_SECRET
```

n8n gửi:

```http
X-Chatbot-Secret: {{$env.CHATBOT_API_SECRET}}
```

Không hardcode secret vào workflow JSON commit lên Git.

### Phạm vi bảo vệ

Ít nhất:

```text
/api/v1/chatbot/orders/track
```

Có thể áp dụng cho memory/log nếu phù hợp nhưng không mở rộng scope quá mức.

### Rate limit

Nếu codebase cho phép, tạo plan riêng cho:

```text
/api/v1/chatbot/orders/track
```

để hạn chế brute-force mã đơn + SĐT.

### Done khi

Request không có secret đúng bị reject.

n8n request hợp lệ vẫn hoạt động.

### Progress report

```text
TASK 8 REPORT

Security mechanism:
...

Environment variable:
...

Unauthorized test:
PASS / FAIL

Authorized test:
PASS / FAIL

Rate limit:
...
```

---

# 6. Test Matrix bắt buộc

Coding agent phải chạy/check các case sau.

## Case 1

```text
Khách: tra cứu đơn hàng
```

Expected:

```text
Bot hỏi số điện thoại.
```

---

## Case 2

```text
Khách: tra cứu đơn hàng
Bot hỏi phone
Khách: 0912345678
```

Expected:

```text
Bot nhớ intent và hỏi mã đơn.
```

---

## Case 3

```text
Khách: tra cứu đơn hàng
Bot hỏi phone
Khách: 0912345678
Bot hỏi mã đơn
Khách: 125
```

Expected:

```text
Call order API.
```

---

## Case 4

```text
Khách: tra cứu đơn 125
```

Expected:

```text
Bot hỏi phone.
```

---

## Case 5

```text
Khách: tra cứu đơn 125 số điện thoại 0912345678
```

Expected:

```text
Call API ngay.
```

---

## Case 6

Sai phone.

Expected:

```text
Không tìm thấy đơn.
Không leak thông tin đơn.
```

---

## Case 7

Sai orderId.

Expected:

```text
Không tìm thấy đơn.
```

---

## Case 8

Message sau khi lookup hoàn tất:

```text
Khách: nike air max còn hàng không?
```

Expected:

```text
Đi đúng product flow.
Không còn bị hiểu là order tracking.
```

---

## Case 9

Pending order state quá 15 phút.

Expected:

```text
Reset state.
Message mới được intent detection bình thường.
```

---

## Case 10

Ollama offline.

Expected:

```text
Order tracking vẫn hoạt động.
```

Đây là test quan trọng.

---

# 7. Regression checklist

Sau khi hoàn thành phải kiểm tra:

- [ ] Telegram Trigger vẫn hoạt động.
- [ ] Non-text message vẫn có fallback.
- [ ] Greeting vẫn hoạt động.
- [ ] Product search vẫn hoạt động.
- [ ] Price intent vẫn hoạt động.
- [ ] Stock intent vẫn hoạt động.
- [ ] FAQ vẫn hoạt động.
- [ ] Policy intents vẫn hoạt động.
- [ ] Memory history vẫn hoạt động.
- [ ] Logging vẫn hoạt động.
- [ ] Rate limit cũ không bị phá.
- [ ] Order tracking không phụ thuộc Ollama.
- [ ] Không expose phone của đơn khác.
- [ ] Không trả đơn nếu `orderId + phone` không khớp.

---

# 8. Quy tắc implementation

Coding agent phải tuân thủ:

1. Không rewrite toàn bộ chatbot nếu không cần.
2. Giữ nguyên các flow đang hoạt động.
3. Thay đổi nhỏ, dễ review.
4. Không hardcode credentials.
5. Không hardcode API secret.
6. Không thay schema Order.
7. Không thay business logic đặt hàng.
8. Không dùng AI để quyết định dữ liệu đơn hàng.
9. Không trả thông tin order nếu phone không khớp.
10. Không tự thêm dependency nếu chưa cần.
11. Build/test sau mỗi nhóm thay đổi backend.
12. Validate JSON workflow sau khi chỉnh n8n.
13. Không commit file chứa credential thật.

---

# 9. Thứ tự thực hiện

Bắt buộc thực hiện theo thứ tự:

```text
TASK 1
  ↓
TASK 2
  ↓
TASK 3
  ↓
TASK 4
  ↓
TASK 5
  ↓
TASK 6
  ↓
TASK 7
  ↓
TASK 8
  ↓
FULL TEST
```

Không làm nhiều task lớn cùng lúc nếu chưa xác nhận task trước hoạt động.

---

# 10. Progress Tracking

Coding agent phải cập nhật bảng này sau mỗi task.

| Task | Nội dung | Status | Files changed | Test |
|---|---|---|---|---|
| 1 | Audit current flow | DONE | - | PASS |
| 2 | Backend state persistence | DONE | ChatMemory.java, ChatMemoryRequest.java, ChatbotService.java, ChatbotController.java, dbTheXuong.sql | PASS (curl memory API) |
| 3 | Parse + merge order state | DONE | n8n: Smart Filter + Intent, Parse Order Tracking (mới), workflow JSON | 17/17 PASS |
| 4 | Order conversation routing | DONE | n8n: Order Routing (IF mới), Kiểm Tra Fetch Đơn (IF mới), workflow JSON | PASS (sim matrix) |
| 5 | Direct order formatter | DONE | n8n: Format Order Response (mới), Build Prompt (bỏ order) | PASS (sim matrix) |
| 6 | Clear + timeout state | DONE | n8n: Parse Order Tracking, Xử Lý Order Reply (mới) | PASS (sim matrix) |
| 7 | Save Memory integration | DONE | n8n: Chuẩn Bị Save Memory | PASS (sim matrix) |
| 8 | Secure order API | DONE | ChatbotController.java, application.yml, .env, n8n: Lấy Dữ Liệu Đơn Hàng (header) | PASS (401/401/200/200) |
| 9 | Full regression test | DONE | workflow-backup.json, sim-core.js, sim-cases.js (temp) | 26/26 PASS |

Status chỉ dùng:

```text
TODO
IN_PROGRESS
BLOCKED
DONE
```

---

# 11. Format báo cáo sau mỗi task

Agent phải trả báo cáo theo format:

```text
## TASK X REPORT

Status:
DONE / BLOCKED

Files changed:
- path/file1
- path/file2

What changed:
- ...
- ...

Tests executed:
- ...

Test result:
PASS / FAIL

Remaining risks:
- ...

Next task:
TASK X+1
```

Nếu BLOCKED:

```text
Blocker:
...

Evidence:
...

Suggested resolution:
...
```

Không được chỉ báo:

```text
done
fixed
completed
```

mà không có file + test result.

---

# 12. Final Report

Sau khi hoàn thành toàn bộ plan, agent phải xuất:

```text
# FINAL IMPLEMENTATION REPORT

## Completed
- ...

## Files changed
- ...

## Backend changes
- ...

## n8n changes
- ...

## Security changes
- ...

## Test results
- Case 1: PASS
- Case 2: PASS
- ...
- Case 10: PASS

## Regression
- Product chatbot: PASS
- FAQ: PASS
- Memory: PASS
- Logging: PASS

## Known limitations
- ...

## Manual setup required
- Environment variables
- Database migration
- n8n credentials/config

## Ready for review
YES / NO
```

---

# 13. Definition of Done

Plan chỉ được xem là hoàn thành khi:

- [ ] Khách có thể tra cứu đơn qua nhiều message.
- [ ] Bot nhớ phone/orderId giữa các message.
- [ ] Bot hỏi đúng field còn thiếu.
- [ ] Có đủ phone + orderId thì API được gọi.
- [ ] Order response không đi qua Ollama.
- [ ] Ollama offline vẫn tra cứu đơn được.
- [ ] State được clear sau khi hoàn tất.
- [ ] State timeout hoạt động.
- [ ] Sai phone không lấy được đơn.
- [ ] API order tracking có lớp bảo vệ phù hợp.
- [ ] Backend build thành công.
- [ ] Workflow n8n JSON hợp lệ.
- [ ] Toàn bộ test matrix được báo cáo.
- [ ] Không làm hỏng product / FAQ / chatbot memory hiện tại.
