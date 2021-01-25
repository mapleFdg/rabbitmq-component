package com.imooc.bo.center;

import lombok.Data;

@Data
public class OrderItemsCommentBO {

    private String commentId;
    private String itemId;
    private String itemName;
    private String itemSpecId;
    private String itemSpecName;
    private Integer commentLevel;
    private String content;

    @Override
    public String toString() {
        return "OrderItemsCommentBO{" +
                "commentLevel=" + commentLevel +
                ", content='" + content + '\'' +
                '}';
    }
}