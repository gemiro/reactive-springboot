package com.example.reactivespringboot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class ReactiveSpringbootController {

    final MongoRepository repository;

    public ReactiveSpringbootController(MongoRepository repository) {
        this.repository = repository;
    }

    @PostMapping(path = "/coffees")
    public Mono<Coffee> postCoffees(@ModelAttribute Mono<Coffee> coffee) {
        return coffee
                .flatMap(c -> {
                    c.setId(UUID.randomUUID().toString());
                    return repository.save(c);
                });
    }

    @GetMapping(path = "/coffees")
    public Flux<Coffee> getCoffees(@RequestParam(name = "name", required = false) String name) {
        return (name == null) ?
                repository.findAll() : repository.findAllByName(name);
    }

    @GetMapping(path = "/coffees/{id}")
    public Mono<Coffee> getCoffee(@PathVariable String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "no data")))
                .doOnError(System.out::println);
    }
}


@Document("coffee")
@Getter
@Setter
class Coffee {
    @Id
    String id;
    String name;
}

@Repository
interface MongoRepository extends ReactiveMongoRepository<Coffee, String> {
    Flux<Coffee> findAllByName(String name);
}