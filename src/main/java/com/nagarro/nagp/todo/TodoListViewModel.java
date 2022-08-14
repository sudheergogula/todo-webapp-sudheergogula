package com.nagarro.nagp.todo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TodoListViewModel {

    private List<TodoItem> todoList;

}