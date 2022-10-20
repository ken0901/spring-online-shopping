package com.ken.app.serviceImpl;

import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.model.Category;
import com.ken.app.model.Product;
import com.ken.app.repository.ProductRepository;
import com.ken.app.service.ProductService;
import com.ken.app.utils.CafeUtils;
import com.ken.app.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    JwtFilter jwtFilter;

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TRUE = "true";
    public static final String CATEGORY_ID = "categoryId";
    public static final String DESCRIPTION = "description";
    public static final String PRICE = "price";
    public static final String STATUS = "status";

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateProductMap(requestMap, false)){
                    productRepository.save(getProductFromMap(requestMap, false));
                    return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_ADDED_SUCCESSFULLY,HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            return new ResponseEntity<>(productRepository.getAllProduct(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateProductMap(requestMap,true)){
                    Optional<Product> optional = productRepository.findById(Integer.parseInt(requestMap.get(ID)));
                    if(!optional.equals(Optional.empty())){
                        Product product = getProductFromMap(requestMap, true);
                        product.setStatus(optional.get().getStatus());
                        productRepository.save(product);
                        return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_UPDATED_SUCCESSFULLY,HttpStatus.OK);
                    }else{
                        return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_ID_DOES_NOT_EXIST,HttpStatus.OK);
                    }
                }else {
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            }else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if (jwtFilter.isAdmin()){
                Optional<Product> optional = productRepository.findById(id);
                if(!optional.equals(Optional.empty())){
                    productRepository.deleteById(id);
                    return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_DELETED_SUCCESSFULLY, HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_ID_DOES_NOT_EXIST, HttpStatus.OK);
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()){
                Optional<Product> optional = productRepository.findById(Integer.parseInt(requestMap.get(ID)));
                if(!optional.equals(Optional.empty())){
                    productRepository.updateProductStatus(requestMap.get(STATUS),Integer.parseInt(requestMap.get(ID)));
                    return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_STATUS_UPDATED_SUCCESSFULLY,HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstants.PRODUCT_ID_DOES_NOT_EXIST,HttpStatus.OK);
            }else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            return new ResponseEntity<>(productRepository.getProductByCategory(id),HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {
            return new ResponseEntity<>(productRepository.getProductById(id),HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get(CATEGORY_ID)));

        Product product = new Product();
        if(isAdd){
            product.setId(Integer.parseInt(requestMap.get(ID)));
        }else{
            product.setStatus(TRUE);
        }
        product.setCategory(category);
        product.setName(requestMap.get(NAME));
        product.setDescription(requestMap.get(DESCRIPTION));
        product.setPrice(Integer.parseInt(requestMap.get(PRICE)));
        return product;
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey(NAME)){
            if(requestMap.containsKey(ID) && validateId){
                return true;
            }else if(!validateId){
                return true;
            }
        }
        return false;
    }
}
