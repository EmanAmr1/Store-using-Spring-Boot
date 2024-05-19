package com.boostmytool.beststore.controllers;

import com.boostmytool.beststore.models.Product;
import com.boostmytool.beststore.models.ProductDto;
import com.boostmytool.beststore.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
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
    public List<Product> AllProducts() {
        return repo.findAll();
    }


    @GetMapping({"", "/"})
    public String ShowProducts(Model model) {
        List<Product> products = repo.findAll();   //store products in list after get all it
        model.addAttribute("products", products);
        return "products/index"; //the html file
    }


   @GetMapping({ "/sort"})
    public String ShowSortedProducts(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));   //store sorted products in list after get all it
        model.addAttribute("products", products);
        return "products/index"; //the html file
    }


    // ***************************** Product ******************************
    @GetMapping({"/getProduct/{id}"})  // to test allProducts in PostMan
    @ResponseBody
    public Product GetProductById(@PathVariable int id) {

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


    // ***************************** create product ******************************

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }


    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){

        //check imageFile manually that it is notEmpty

        if(productDto.getImageFile() == null ||productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto" ,"imageFile" ,"the file is required"));
        }

        if(result.hasErrors()){
            return "products/createProduct";
        }


        //save image file

        MultipartFile image = productDto.getImageFile(); //Read image from dto
        Date createdAt = new Date();  //read the date
        String storageFileName =image.getOriginalFilename(); //create unique fileName to this image

        try{
            String uploadDir ="public/images/"; //folder to save image
            Path uploadPath = Paths.get(uploadDir); //obj of type path

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream=image.getInputStream()){  //obj
                Files.copy(inputStream,Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (Exception ex){
            System.out.println("Exception: "+ex.getMessage());
        }

        //to save product in the db

        Product p = new Product();  //take obj from product to save the  new product on it
        p.setName(productDto.getName());
        p.setBrand(productDto.getBrand());
        p.setCategory(productDto.getCategory());
        p.setPrice(productDto.getPrice());
        p.setDescription(productDto.getDescription());
        p.setCreatedAt(createdAt);   // the current date
        p.setImageFileName(storageFileName); //the uploaded file

        repo.save(p);




        return "redirect:/products";
    }


    // ***************************** edit product ******************************

@GetMapping("/edit")
    public String ShowEditPage(Model model, @RequestParam int id){

        try{
            Product p =repo.findById(id).get();
            model.addAttribute("product", p);

            ProductDto pd = new ProductDto();  //take obj from productDto to save the  edited product on it
            pd.setName(p.getName());
            pd.setBrand(p.getBrand());
            pd.setCategory(p.getCategory());
            pd.setPrice(p.getPrice());
            pd.setDescription(p.getDescription());

            model.addAttribute("productDto" ,pd);
        }
        catch (Exception ex){
            System.out.println("Exception: " + ex.getMessage());
            return "redirect/products";
        }

        return "products/EditProduct";
}



@PostMapping("/edit")
    public String updateProduct(Model model ,
                                @RequestParam int id,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult result){

        try {
            Product p =repo.findById(id).get();
            model.addAttribute("product",p);


            //check if there is any error
            if(result.hasErrors()){
                return "products/EditProduct";
            }

            //check the image

            if(!productDto.getImageFile().isEmpty()){
                //delete old image

                String uploadDir ="public/images";
                Path oldImagePath =Paths.get(uploadDir + p.getImageFileName());
                        try{
                           Files.delete(oldImagePath);

                        }catch (Exception ex){
                            System.out.println("Execption: "+ex.getMessage());

                        }


                        //save new image file
                MultipartFile image =productDto.getImageFile();
                Date createdAt = new Date();  //read the date
                String storageFileName =image.getOriginalFilename(); //create unique fileName to this image

                    try (InputStream inputStream=image.getInputStream()){  //obj
                        Files.copy(inputStream,Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                    }

                p.setImageFileName(storageFileName); //the uploaded file



            }
            p.setName(productDto.getName());
            p.setBrand(productDto.getBrand());
            p.setCategory(productDto.getCategory());
            p.setPrice(productDto.getPrice());
            p.setDescription(productDto.getDescription());

            repo.save(p);

        }catch (Exception ex){
            System.out.println("Exception: "+ex.getMessage());       }


        return "redirect:/products";
}




    // ***************************** delete product ******************************


    @GetMapping("/delete")
    public String deleteProduct (@RequestParam int id) {
        try {
            Product p = repo.findById(id).get();

            //delete product image
            Path imagePath = Paths.get("public/images/" + p.getImageFileName());

            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());

            }
        //delete the product from db
            repo.delete(p);

        }
            catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());

            }
            return "redirect:/products";
        }




}