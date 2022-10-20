package com.ken.app.serviceImpl;

import com.ken.app.constants.CafeConstants;
import com.ken.app.repository.BillRepository;
import com.ken.app.service.BillService;
import com.ken.app.utils.CafeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillRepository billRepository;

    @Override
    public ResponseEntity<String> generateReport(Map<String, String> requestMap) {
        try {

        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
