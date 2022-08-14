package com.nagarro.nagp.todo;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class TodoItem {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String category;
    private String name;
    private boolean complete;

}