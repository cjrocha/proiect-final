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
public class AltexScraper implements  Runnable {
    private static Logger logger = LoggerFactory.getLogger(AltexScraper.class);
    private final ChromeDriver driver;

    @Autowired
    private SearchRepository searchTerms;
    @Autowired
    private ProductRepository productRepository;


    public void AltScraper() {
        LocalDateTime now = LocalDateTime.now();
        Long termId = 0L;
        String termUrl = null;
        List<SearchTerms> terms = searchTerms.findAllByOrderByIdDesc();
        boolean test = true;
        for (SearchTerms term : terms) {
            while (test) {
                if (term.getSource().equals("altex")) {
                    termId = term.getSearchID();
                    termUrl = term.getTermUrl();
                    logger.info("search_id is: " + termId + " The term url is: " + termUrl);
                    test = false;
                }
            }
        }
        //visit url and get number of pages
        Browser pagesCatcherBrowser = new Browser(driver);
        pagesCatcherBrowser.visit(termUrl);
        String entries = "0";
        int nentries = 0;
        try {
            entries = pagesCatcherBrowser.doc.findFirst("<div class=text-sm.font-medium.text-center.md:mt-2.py-2>").getText();
            nentries = Integer.parseInt(entries.split(" ")[2]);
        } catch (NotFound e) {
            nentries = 0;
            e.printStackTrace();
        }
        pagesCatcherBrowser.close();
        int pages = nentries / 24 + 1;
        String url = "";
        if (pages > 2) {
            url = termUrl + "?page=2";
        } else {
            url = termUrl;
        }
        //visit product pages and get data
        List<String> prodDetUrl = new ArrayList<>();
        Browser pagesNewCatcherBrowser = new Browser(new ChromeDriver());
        pagesNewCatcherBrowser.visit(url);
        try {
            Element block = pagesNewCatcherBrowser.doc.findFirst("<div class=lg:w-4/5 ");
            Elements products = block.findEach("<li>");
            for (Element product : products) {
                String pUrl = product.findFirst("<a>").getAttribute("href");
                prodDetUrl.add(pUrl);
            }
            pagesNewCatcherBrowser.close();
        } catch (JauntiumException e) {
            pagesNewCatcherBrowser.close();
        }
        for (String pUrl : prodDetUrl) {
            Browser productCatcherBrowser = new Browser(new ChromeDriver());
            productCatcherBrowser.visit(pUrl);
            Product product = new Product();
            try {
                String stock = "";
                try {
                    stock = productCatcherBrowser.doc.findFirst("<div class=flex.items-center.text-green.text-13px.leading-tight.-tracking-0.39.mb-6>").getText();
                } catch (NotFound e) {
                    stock = "stock epuizat";
                }
                Element forPrice = productCatcherBrowser.doc.findFirst("<div class=my-2>").getElement(0);
                String oldPrice, price;
                try {
                    price = forPrice.getElement(1).getText().replace(".", "").split(" ")[0].trim();;
                    oldPrice = forPrice.getElement(0).getElement(0).getText().replace(".", "").split(" ")[0].trim();;
                } catch (ArrayIndexOutOfBoundsException e) {
                    price = forPrice.getElement(0).getElement(0).getText().replace(".", "").split(" ")[0].trim();;
                    oldPrice = "0";
                }
                product.setProductName(productCatcherBrowser.doc.findFirst("<div class=mb-1>").getElement(0).getText());
                product.setProductSource("altex");
                product.setProductBrand("");
                product.setProductUrl(productCatcherBrowser.getLocation());
                product.setProductId(productCatcherBrowser.getLocation().split("/")[5]);
                product.setProductSku(productCatcherBrowser.doc.findFirst("<div class=inline-block.p-1.text-xs.md:text-sm.font-semibold.rounded-sm.bg-gray-300>").getText().replace("Cod produs: ", ""));
                product.setProductMainImage(productCatcherBrowser.doc.findFirst("<div class=swiper-wrapper>").getElement(0).getElement(0).getElement(0).getAttribute("src"));
                product.setProductStock(stock);
                product.setOldPrice(Double.parseDouble(oldPrice.replace(",", ".")));
                product.setPrice(Double.parseDouble(price.replace(",", ".")));
                product.setProductDescription(productCatcherBrowser.doc.findFirst("<li id=description>").getElement(1).getText());
                product.setAddedOn(now);
                product.setTermId(termId);
                productRepository.save(product);
                productCatcherBrowser.close();

            } catch (NotFound nf) {
                logger.error(Arrays.toString(nf.getStackTrace()) + ". One or more elements were not found!");
                productCatcherBrowser.close();
            }
        }
        logger.info("Altex scraper has ended!");
    }

    @Override
    public void run() {AltScraper();}
}
