package com.ken.app.serviceImpl;

import com.google.common.base.Strings;
import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.model.Category;
import com.ken.app.repository.CategoryRepository;
import com.ken.app.service.CategoryService;
import com.ken.app.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    JwtFilter jwtFilter;

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TRUE = "true";

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap,false)){
                    categoryRepository.save(getCategoryFromMap(requestMap,false));
                    return CafeUtils.getResponseEntity(CafeConstants.CATEGORY_ADDED_SUCCESSFULLY, HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase(TRUE)){
                log.info("Inside getAllCategory");
                return new ResponseEntity<>(categoryRepository.getAllCategory(),HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap,true)){
                    Optional<Category> optional = categoryRepository.findById(Integer.parseInt(requestMap.get(ID)));
                    if(!(optional.equals(Optional.empty()))){
                        categoryRepository.save(getCategoryFromMap(requestMap, true));
                        return CafeUtils.getResponseEntity(CafeConstants.CATEGORY_UPDATED_SUCCESSFULLY,HttpStatus.OK);
                    }else{
                        return CafeUtils.getResponseEntity(CafeConstants.CATEGORY_ID_DOES_NOT_EXIST, HttpStatus.OK);
                    }
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey(NAME)){
            if(requestMap.containsKey(ID) && validateId){
                return true;
            }else if(!validateId){
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap,Boolean isAdd){
        Category category = new Category();
        if(isAdd){
            category.setId(Integer.parseInt(requestMap.get(ID)));
        }
        category.setName(requestMap.get(NAME));
        return category;
    }
}
