package com.example.taskbackend.model; // 和目录对应

import jakarta.validation.constraints.NotBlank;

public class OwlRequest {

    @NotBlank(message = "ticker不能为空")
    private String ticker;

    @NotBlank(message = "model不能为空")
    private String model;

    // getter 和 setter
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
}
