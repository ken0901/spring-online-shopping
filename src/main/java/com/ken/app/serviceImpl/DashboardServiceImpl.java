package com.ken.app.serviceImpl;

import com.ken.app.repository.BillRepository;
import com.ken.app.repository.CategoryRepository;
import com.ken.app.repository.ProductRepository;
import com.ken.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BillRepository billRepository;

    private static final String CATEGORY = "category";
    private static final String PRODUCT = "product";
    private static final String BILL = "bill";

    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        Map<String, Object> map = new HashMap<>();
        map.put(CATEGORY,categoryRepository.count());
        map.put(PRODUCT,productRepository.count());
        map.put(BILL,billRepository.count());
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
