package com.example.thexuong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages")
public class PageController {

    @GetMapping("/size-guide")
    public String sizeGuide() {
        return "pages/size-guide";
    }

    @GetMapping("/return-policy")
    public String returnPolicy() {
        return "pages/return-policy";
    }

    @GetMapping("/payment-methods")
    public String paymentMethods() {
        return "pages/payment-methods";
    }

    @GetMapping("/order-tracking")
    public String orderTracking() {
        return "pages/order-tracking";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "pages/privacy-policy";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "pages/terms-of-service";
    }

    @GetMapping("/shipping-policy")
    public String shippingPolicy() {
        return "pages/shipping-policy";
    }

    @GetMapping("/partnership")
    public String partnership() {
        return "pages/partnership";
    }
}
