package com.ken.app.serviceImpl;

import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.model.Bill;
import com.ken.app.repository.BillRepository;
import com.ken.app.service.BillService;
import com.ken.app.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    JwtFilter jwtFilter;

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TRUE = "true";
    public static final String CONTACT_NUMBER = "contactNumber";
    public static final String EMAIL = "email";
    public static final String PAYMENT_METHOD = "paymentMethod";
    public static final String PRODUCT_DETAILS = "productDetails";
    public static final String TOTAL_AMOUNT = "totalAmount";
    private static final String IS_GENERATE = "isGenerat";
    private static final String UUID = "uuid";

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside of generateReport");
        try {
            String fileName;
            if(validateRequestMap(requestMap)){
                if(requestMap.containsKey(IS_GENERATE) && !(Boolean) requestMap.get(IS_GENERATE)){
                    fileName = (String) requestMap.get(UUID);
                }else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid",fileName);
                    insertBill(requestMap);
                }
            }
            return CafeUtils.getResponseEntity(CafeConstants.REQUIRED_DATA_NOT_FOUND,HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get(UUID));
            bill.setName((String) requestMap.get(NAME));
            bill.setEmail((String) requestMap.get(EMAIL));
            bill.setContactNumber((String) requestMap.get(CONTACT_NUMBER));
            bill.setPaymentMethod((String) requestMap.get(PAYMENT_METHOD));
            bill.setTotal((Integer) requestMap.get(TOTAL_AMOUNT));
            bill.setProductDetails((String) requestMap.get(PRODUCT_DETAILS));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey(NAME) &&
                requestMap.containsKey(CONTACT_NUMBER) &&
                requestMap.containsKey(EMAIL) &&
                requestMap.containsKey(PAYMENT_METHOD) &&
                requestMap.containsKey(PRODUCT_DETAILS) &&
                requestMap.containsKey(TOTAL_AMOUNT);
    }
}
