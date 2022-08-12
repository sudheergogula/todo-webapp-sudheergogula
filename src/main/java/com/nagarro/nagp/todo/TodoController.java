package com.nagarro.nagp.todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TodoController {

    @Autowired
    private TodoItemRepository repository;

    @RequestMapping("/")
    public String index(Model model) {
        repository.deleteAll();
        List<TodoItem> todoList = new ArrayList<>();
        model.addAttribute("newitem", new TodoItem());
        model.addAttribute("items", new TodoListViewModel(todoList));
        return "index";
    }

    @RequestMapping("/task")
    public String task(Model model) {
        List<TodoItem> todoList = (ArrayList<TodoItem>) repository.findAll();
        model.addAttribute("newitem", new TodoItem());
        model.addAttribute("items", new TodoListViewModel(todoList));
        return "task";
    }

    @RequestMapping("/add")
    public String addTodo(@ModelAttribute TodoItem requestItem) {
        repository.save(newTodoItem(requestItem));
        return "redirect:/task";
    }

    @RequestMapping("/update")
    public String updateTodo(@ModelAttribute TodoListViewModel requestItems) {
        if (requestItems != null && requestItems.getTodoList() != null) {
            for (TodoItem requestItem : requestItems.getTodoList()) {
                TodoItem item = newTodoItem(requestItem);
                item.setComplete(requestItem.isComplete());
                item.setId(requestItem.getId());
                repository.save(item);
            }
        }
        return "redirect:/task";
    }

    private TodoItem newTodoItem(TodoItem requestItem) {
        TodoItem item = new TodoItem();
        item.setCategory(requestItem.getCategory());
        item.setName(requestItem.getName());
        return item;
    }

}
