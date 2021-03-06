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
import java.util.UUID;

/**
 * External source product scraper
 * based on start url provided by category crawler.
 * It's implementing runnable class in order to
 * offer multi-threading capability.
 */
@Service
@AllArgsConstructor
public class EmagScraper implements Runnable{
    private static Logger logger = LoggerFactory.getLogger(EmagScraper.class);
    private final ChromeDriver driver;

    @Autowired
    private SearchRepository searchTerms;
    @Autowired
    private ProductRepository productRepository;

    /**
     * Category pages crawler and grabber of the number of listing pages.
     * Crawler of listing pages and grabber of products data.
     * Uses chrome driver, selenium and jauntium libraries,
     * to visit the web pages and gather data needed.
     * Based on search term provided by user, builds the search url,
     * visits the page and follows all pagination's, then moves to each paginated
     * page and follows products links to access and grab the product data.
     * Data gathered is being pushed to DB.
     * Catches exceptions and handles them.
     * Logs exceptions and success operations.
     */
    public void EmgScraper() {
        LocalDateTime now = LocalDateTime.now();
        Long eTermId = 0L;
        String eTermUrl = null;
        boolean test = true;
        List<SearchTerms> terms = searchTerms.findAllByOrderByIdDesc();

        //obtain the url to crawl and the term that was searched
        for (SearchTerms term : terms) {
            while (test) {
                if (term.getSource().equals("emag")) {
                    eTermId = term.getSearchID();
                    eTermUrl = term.getTermUrl();
                    logger.info("search_id is: " + eTermId + " The term url is: " + eTermUrl);
                    test = false;
                }
            }
        }

        //visit url and add product urls to a list using follow pagination
        List<String> prodDetUrl = new ArrayList<>();
        boolean check = true;
        int s = 2;
        Browser productsCatcherBrowser = new Browser(new ChromeDriver());
        try {
            while (check) {
                productsCatcherBrowser.visit(eTermUrl);
                Element grid = productsCatcherBrowser.doc.findFirst("<div id=card_grid>");
                String fPath = "<div class=card-v2>";
                Elements products = grid.findEach(fPath);
                for (Element product : products) {
                    String path = "<a class=card-v2-title.semibold.mrg-btm-xxs.js-product-url>";
                    String pUrl = product.findFirst(path).getAttribute("href");
                    prodDetUrl.add(pUrl);
                }
                String nextPath = "<a data-page=" + s + ">";
                eTermUrl = productsCatcherBrowser.doc.findFirst(nextPath).getAttribute("href");
                s++;
            }
        } catch (JauntiumException e) {
            check = false;
        }
        productsCatcherBrowser.close();

        //gather products data
        Product product = new Product();
        for (String eProdUrl : prodDetUrl) {
            Browser detailsBrowser = new Browser(new ChromeDriver());
            detailsBrowser.visit(eProdUrl);
            try {
                String price, oldPrice;
                try {
                    oldPrice = detailsBrowser.doc.findFirst("<form class=main-product-form>").getElement(0).getElement(0).getElement(0).getElement(0).getElement(0).getText()
                            .replace(".", "").replace("PRP:", "").split(" ")[0].trim();
                    ;
                    price = detailsBrowser.doc.findFirst("<form class=main-product-form>").getElement(0).getElement(0).getElement(0).getElement(0).getElement(1).getText()
                            .replace(".", "").split(" ")[0].trim();
                    ;
                } catch (com.jauntium.NotFound e) {
                    price = detailsBrowser.doc.findFirst("<form class=main-product-form>").getElement(0).getElement(0).getElement(0).getElement(0).getElement(1).getText()
                            .replace(".", "").split(" ")[0].trim();
                    ;
                    oldPrice = "0";
                } catch (ArrayIndexOutOfBoundsException ae){
                    oldPrice ="0";
                    price = detailsBrowser.doc.findFirst("<form class=main-product-form>").getElement(0).getElement(0).getElement(0).getElement(0).getElement(1).getText()
                            .replace(".", "").split(" ")[0].trim();
                }
                String description;
                try {
                    description = detailsBrowser.doc.findFirst("<div id=collapse-wrapper-description>").getText().replace("Vezi mai mult\nVezi si: Genius eMAG", "").trim();
                } catch (com.jauntium.NotFound e) {
                    description = detailsBrowser.doc.findFirst("<h1 class=page-title>").getText();
                }
                product.setpId(UUID.randomUUID().toString());
                product.setProductName(detailsBrowser.doc.findFirst("<h1 class=page-title>").getText());
                product.setProductUrl(detailsBrowser.getLocation());
                product.setProductId(detailsBrowser.doc.findFirst("<form class=main-product-form>").getAttribute("data-product-id"));
                product.setProductSku(detailsBrowser.doc.findFirst("<span class=product-code-display>").getText().replace("Cod produs: ", "").trim());
                if(oldPrice == ""){
                    oldPrice = "0";
                } else {
                    oldPrice = oldPrice.replace(",", ".");
                }
                product.setOldPrice(Double.parseDouble(oldPrice));
                product.setPrice(Double.parseDouble(price.replace(",", ".")));
                product.setProductDescription(description);
                product.setProductStock(detailsBrowser.doc.findFirst("<div class=stock-and-genius>").getText());
                product.setProductBrand(detailsBrowser.doc.findFirst("<div class=disclaimer-section.mrg-sep-sm>").getElement(0).getElement(0).getText());
                product.setProductMainImage(detailsBrowser.doc.findFirst("<div class=ph-body>").getElement(0).getElement(0).getAttribute("href"));
                product.setProductSource("emag");
                product.setTermId(eTermId);
                product.setAddedOn(now);
                productRepository.save(product);
                detailsBrowser.close();
            } catch (NotFound ntf) {
                logger.error(Arrays.toString(ntf.getStackTrace()) + ". Some elements were not found");

            }
        }
        logger.info("Scraperul de produse Emag a terminat treaba");
    }

    /**
     * Override of run method to
     * handle product scraper
     */
    @Override
    public void run() {
        EmgScraper();
    }
}
