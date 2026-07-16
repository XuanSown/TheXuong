package com.example.thexuong.service;

import com.example.thexuong.dto.ChatbotFaqDto;
import com.example.thexuong.dto.ChatbotProductDto;
import com.example.thexuong.dto.ChatLogRequest;
import com.example.thexuong.dto.ChatMemoryRequest;
import com.example.thexuong.entity.Faq;
import com.example.thexuong.entity.ChatLog;
import com.example.thexuong.entity.ChatMemory;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.FaqRepository;
import com.example.thexuong.repository.ChatLogRepository;
import com.example.thexuong.repository.ChatMemoryRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.dto.ChatbotOrderDto;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

	private final ProductRepository productRepository;
	private final FaqRepository faqRepository;
	private final ChatMemoryRepository chatMemoryRepository;
	private final ChatLogRepository chatLogRepository;
	private final ProductVariantRepository productVariantRepository;
	private final OrderRepository orderRepository;

	// ==================== Products ====================

	/**
	 * Get all active products for chatbot context.
	 * Returns simplified DTOs with stock status.
	 */
	@Transactional(readOnly = true)
	public List<ChatbotProductDto> getAllProductsForChatbot() {
		List<Product> products = productRepository.findAll();
		return products.stream()
				.map(this::toChatbotProductDto)
				.toList();
	}

	private ChatbotProductDto toChatbotProductDto(Product product) {
		// Load variants explicitly to avoid LazyInitializationException
		List<ProductVariant> variants = product.getVariants();
		if (variants == null || variants.isEmpty()) {
			variants = productVariantRepository.findByProductId(product.getId());
		}
		String stockStatus = calculateStockStatus(variants);

		return ChatbotProductDto.builder()
				.id(product.getId())
				.name(product.getName())
				.category(product.getCategory() != null ? product.getCategory().getName() : null)
				.price(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0)
				.sport(product.getSport() != null ? product.getSport().getName() : null)
				.brand(product.getBrand() != null ? product.getBrand().getName() : null)
				.description(product.getDescription())
				.stockStatus(stockStatus)
				.build();
	}

	private String calculateStockStatus(List<ProductVariant> variants) {
		if (variants == null || variants.isEmpty()) {
			return "Không rõ";
		}
		int totalStock = variants.stream()
				.mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
				.sum();
		if (totalStock == 0) {
			return "Hết hàng";
		} else if (totalStock <= 5) {
			return "Sắp hết";
		} else {
			return "Còn hàng";
		}
	}

	// ==================== FAQs ====================

	/**
	 * Get all FAQs for chatbot context.
	 */
	@Transactional(readOnly = true)
	public List<ChatbotFaqDto> getAllFaqsForChatbot() {
		List<Faq> faqs = faqRepository.findAllByOrderByTopicAscIdAsc();
		return faqs.stream()
				.map(faq -> ChatbotFaqDto.builder()
						.id(faq.getId())
						.topic(faq.getTopic())
						.questionKeywords(faq.getQuestionKeywords())
						.answer(faq.getAnswer())
						.build())
				.toList();
	}

	// ==================== Chat Memory ====================

	/**
	 * Get conversation history for a chat_id.
	 */
	@Transactional(readOnly = true)
	public String getChatMemory(String chatId) {
		ChatMemory memory = chatMemoryRepository.findById(chatId).orElse(null);
		if (memory == null) {
			return "[]";
		}
		return memory.getHistoryJson();
	}

	/**
	 * Save or update conversation history for a chat_id.
	 */
	@Transactional
	public void saveChatMemory(String chatId, String historyJson) {
		ChatMemory memory = chatMemoryRepository.findById(chatId).orElse(null);
		if (memory == null) {
			memory = ChatMemory.builder()
					.chatId(chatId)
					.historyJson(historyJson)
					.build();
		} else {
			memory.setHistoryJson(historyJson);
		}
		chatMemoryRepository.save(memory);
	}

	// ==================== Chat Logs ====================

	/**
	 * Log a chatbot interaction.
	 */
	// ==================== Order Tracking ====================

	@Transactional(readOnly = true)
	public ChatbotOrderDto trackOrder(Long orderId, String phoneNumber) {
		Order order = orderRepository.findByIdAndPhoneNumberWithDetails(orderId, phoneNumber).orElse(null);
		if (order == null) return null;

		List<String> items = order.getOrderDetails().stream()
				.map(detail -> detail.getQuantity() + "x " + detail.getProductName() + " (Size " + detail.getSize() + ")")
				.toList();

		return ChatbotOrderDto.builder()
				.orderId(order.getId())
				.status(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN")
				.totalMoney(order.getTotalMoney())
				.paymentMethod(order.getPaymentMethod())
				.createdAt(order.getCreatedAt())
				.items(items)
				.build();
	}

	// ==================== Chat Logs ====================

	/**
	 * Log a chatbot interaction.
	 */
	@Transactional
	public void logInteraction(ChatLogRequest request) {
		ChatLog log = ChatLog.builder()
				.chatId(request.getChatId())
				.userName(request.getUserName())
				.intent(request.getIntent())
				.userMessage(request.getUserMessage())
				.botReply(request.getBotReply())
				.build();
		chatLogRepository.save(log);
	}
}
