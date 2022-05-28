package com.unpontdev.comparator.services;

import com.jauntium.*;
import com.unpontdev.comparator.entities.Product;
import com.unpontdev.comparator.entities.SearchTerms;
import com.unpontdev.comparator.repositories.ProductRepository;
import com.unpontdev.comparator.repositories.SearchRepository;
import lombok.AllArgsConstructor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class FlancoScraper implements  Runnable{
    private static Logger logger = LoggerFactory.getLogger(FlancoScraper.class);
    private final ChromeDriver driver;

    @Autowired
    private SearchRepository searchTerms;
    @Autowired
    private ProductRepository productRepository;


    public void FlcScraper() {
        LocalDateTime now = LocalDateTime.now();
        Long termId = 0L;
        String termUrl = null;
        List<SearchTerms> terms = searchTerms.findAllByOrderByIdDesc();
        boolean test = true;
        for(SearchTerms term : terms){
            while(test){
                if(term.getSource().equals("flanco")) {
                    termId = term.getSearchID();
                    termUrl = term.getTermUrl();
                    logger.info("search_id is: "+termId+" The term url is: "+termUrl);
                    test = false;
                }
            }
        }
        //visit url and add product urls to a list
        Browser pagesCatcherBrowser = new Browser(new ChromeDriver());
        pagesCatcherBrowser.visit(termUrl);
        List<String> prodDetUrl = new ArrayList<>();
        boolean check = true;
        try {
            while (check) {
                Elements products = pagesCatcherBrowser.doc.findEach("<div class=product-item-info>");
                for (Element product : products) {
                    String pUrl = product.findFirst("<a class=product-item-link>").getAttribute("href");
                    prodDetUrl.add(pUrl);
                }
                String nextPage = pagesCatcherBrowser.doc.findFirst("<a title=Următorul>").getAttribute("href");
                pagesCatcherBrowser.visit(nextPage);
            }
        }catch(JauntiumException e){
            check = false;
        }
        pagesCatcherBrowser.close();
        //visit each product page and get data required
        for (String prodUrl : prodDetUrl) {
            Product product = new Product();
            Browser detBrowser = new Browser(new ChromeDriver());
            detBrowser.visit(prodUrl);
            int stock;
            try {
                //handle data
                String price, oldPrice;
                try {
                    oldPrice = detBrowser.doc.findFirst("<div class=pricesPrp>").getElement(0).getElement(0).getElement(1).getText();
                    oldPrice = oldPrice.replace(".", "").split(" ")[0].trim();
                    price = detBrowser.doc.findFirst("<div class=pricesPrp>").getElement(1).getElement(0).getText();
                    price = price.replace(".", "").split(" ")[0].trim();
                } catch (Exception e) {
                    price = detBrowser.doc.findFirst("<div class=price-box.price-final_price>").getElement(0).getText();
                    price = price.replace(".", "").split(" ")[0].trim();
                    oldPrice = "0";
                }
                String sstock = detBrowser.doc.findFirst("<div class=stock>").getText();
                if (!sstock.equals("In stock") || !sstock.equals("Stock limitat")) {
                    stock = 1;
                } else {
                    stock = 0;
                }
                String[] brands = detBrowser.doc.findFirst("<body id=html-body>").getElement(5).innerHTML().split(",");
                String brand = brands[4].replace("bc:\"", "").replace("\"\n", "").replace("};", "").trim();
                //handle process over
                product.setProductName(detBrowser.doc.findFirst("<h1 class=page-title>").getText());
                product.setProductUrl(detBrowser.getLocation());
                product.setProductId(detBrowser.doc.findFirst("<div class=product-info-stock-sku>").getElement(0).getElement(1).getText());
                product.setProductSku(detBrowser.doc.findFirst("<div class=product-info-stock-sku>").getElement(1).getElement(1).getText());
                product.setPrice(Double.parseDouble(price.replace(",", ".")));
                product.setOldPrice(Double.parseDouble(oldPrice.replace(",", ".")));
                try {
                    product.setProductDescription(detBrowser.doc.findFirst("<div class=contorsy-st>").getText());
                } catch(NotFound nfd){
                    product.setProductDescription(detBrowser.doc.findFirst("<h1 class=page-title>").getText());
                }
                product.setProductStock(String.valueOf(stock));
                product.setProductMainImage(detBrowser.doc.findFirst("<figure class=slick-slide.slick-current.slick-active>").getElement(0).getAttribute("href"));
                product.setProductBrand(brand);
                product.setProductSource("flanco");
                product.setTermId(termId);
                product.setAddedOn(now);
                productRepository.save(product);
                detBrowser.close();

            } catch (NotFound nf) {
                logger.error(Arrays.toString(nf.getStackTrace()) + ". One or more elements were not found!");
                detBrowser.close();
            } catch (Exception e) {
                logger.error(Arrays.toString(e.getStackTrace()) +"One element was broken!");
                detBrowser.close();
            }
        }
        logger.info("Flanco scraper has ended!");
    }

    @Override
    public void run() {
        FlcScraper();
    }
}
