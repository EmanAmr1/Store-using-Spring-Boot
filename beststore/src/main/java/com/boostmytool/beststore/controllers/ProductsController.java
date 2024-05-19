package com.boostmytool.beststore.controllers;

import com.boostmytool.beststore.models.Product;
import com.boostmytool.beststore.services.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;


    // ***************************** allProducts ******************************

    @GetMapping({"/myproducts"})  // to test allProducts in PostMan
    @ResponseBody
    public List<Product> AllProducts (){
        return repo.findAll();
    }


    @GetMapping({"","/"})
    public String ShowProducts(Model model){
        List<Product> products =repo.findAll();   //store products in list after get all it
        model.addAttribute("products",products);
        return "products/index"; //the html file
    }


    @GetMapping({"","/sort"})
    public String ShowSortedProducts(Model model){
        List<Product> products =repo.findAll(Sort.by(Sort.Direction.DESC ,"id"));   //store sorted products in list after get all it
        model.addAttribute("products",products);
        return "products/index"; //the html file
    }



    // ***************************** Product ******************************
    @GetMapping({"/getProduct/{id}"})  // to test allProducts in PostMan
    @ResponseBody
    public Product GetProductById (@PathVariable int id){

        return repo.findById(id).get();
    }
    @GetMapping("/getOneProduct/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Optional<Product> product = repo.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }





}
