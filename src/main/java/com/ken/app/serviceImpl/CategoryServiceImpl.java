package com.ken.app.serviceImpl;

import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.model.Category;
import com.ken.app.repository.CategoryRepository;
import com.ken.app.service.CategoryService;
import com.ken.app.utils.CafeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    JwtFilter jwtFilter;

    public static final String NAME = "name";
    public static final String ID = "id";

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
