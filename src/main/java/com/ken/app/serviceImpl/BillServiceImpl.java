package com.ken.app.serviceImpl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.model.Bill;
import com.ken.app.repository.BillRepository;
import com.ken.app.service.BillService;
import com.ken.app.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    JwtFilter jwtFilter;

    public static final String NAME = "name";
    public static final String UPPER_NAME = "Name";
    public static final String CONTACT_NUMBER = "contactNumber";
    public static final String EMAIL = "email";
    public static final String PAYMENT_METHOD = "paymentMethod";
    public static final String PRODUCT_DETAILS = "productDetails";
    public static final String TOTAL_AMOUNT = "totalAmount";
    private static final String IS_GENERATE = "isGenerat";
    private static final String UUID = "uuid";
    private static final String CAFE_MANAGEMENT_SYSTEM = "Cafe Management System";
    private static final String HEADER = "header";
    private static final String DATA = "data";
    private static final String UP_CATEGORY = "Category";
    private static final String CATEGORY = "category";
    private static final String QUANTITY = "quantity";
    private static final String PRICE = "price";
    private static final String SUB_TOTAL = "Sub Total";
    private static final String TOTAL = "total";

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
                String data = "Name: "+requestMap.get(NAME) + "\n"
                            + "Contact Number: "+ requestMap.get(CONTACT_NUMBER) + "\n"
                            + "Email: " + requestMap.get(EMAIL) + "\n"
                            + "Payment Method: " + requestMap.get(PAYMENT_METHOD) + "\n";
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION+"\\"+fileName+".pdf"));

                document.open();
                setRectangleInPdf(document);

                Paragraph chunk = new Paragraph(CAFE_MANAGEMENT_SYSTEM,getFont(HEADER));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);

                Paragraph paragraph = new Paragraph(data+"\n \n",getFont(DATA));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get(PRODUCT_DETAILS));
                for (int i=0; i<jsonArray.length(); i++){
                    addRows(table,CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total: " + requestMap.get(TOTAL_AMOUNT)+"\n"
                                                +"Thank you for visiting.Please visit again!!",getFont(DATA));
                document.add(footer);
                document.close();
                return new ResponseEntity<>("{\"uuid\""+fileName+"\"}",HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity(CafeConstants.REQUIRED_DATA_NOT_FOUND,HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows");
        table.addCell((String) data.get(NAME));
        table.addCell((String) data.get(CATEGORY));
        table.addCell((String) data.get(QUANTITY));
        table.addCell(Double.toString((Double) data.get(PRICE)));
        table.addCell(Double.toString((Double) data.get(TOTAL)));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader");
        Stream.of(UPPER_NAME,UP_CATEGORY,QUANTITY,PRICE,SUB_TOTAL)
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type){
            case HEADER:
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18,BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case DATA:
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf");
        Rectangle rect = new Rectangle(577,825,18,15);
        rect.enableBorderSide(1);
        rect.enableBorderSide(2);
        rect.enableBorderSide(4);
        rect.enableBorderSide(8);
        rect.setBorderColor(BaseColor.BLACK);
        rect.setBorderWidth(1);
        document.add(rect);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get(UUID));
            bill.setName((String) requestMap.get(NAME));
            bill.setEmail((String) requestMap.get(EMAIL));
            bill.setContactNumber((String) requestMap.get(CONTACT_NUMBER));
            bill.setPaymentMethod((String) requestMap.get(PAYMENT_METHOD));
            bill.setTotal(Integer.parseInt((String) requestMap.get(TOTAL_AMOUNT)));
            bill.setProductDetails((String) requestMap.get(PRODUCT_DETAILS));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billRepository.save(bill);
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
