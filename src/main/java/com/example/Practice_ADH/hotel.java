package com.example.Practice_ADH;

import org.springframework.web.bind.annotation.GetMapping;

public class hotel {
    @GetMapping("/hotelbooking")
    public String getName() {
        return "Book your hotel as soon as possible";
    }
    public String getName1() {
        return "Book your hotel as soon as possible";
    }
}
