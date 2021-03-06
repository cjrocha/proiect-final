package com.unpontdev.comparator.controler;

import com.unpontdev.comparator.entities.Product;
import com.unpontdev.comparator.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Takes care of building data and showing
 * the sorted product pages
 */
@Controller
public class ProductOrderController {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Sort by name page
     * @param model - html handler
     * @return - orderbyname
     */
    @RequestMapping(value = "/searchlists/orderbyname", method = RequestMethod.GET)
    public String showOrderByName(Model model) {
        List<Product> productsOrderByName = productRepository.findAll(Sort.by(Sort.Direction.ASC, "productName"));
        model.addAttribute("productsOrderByName", productsOrderByName);
        return "searchlists/orderbyname";
    }

    /**
     * Sort by sku page
     * @param model - html handler
     * @return - orderbysku
     */
    @RequestMapping(value = "/searchlists/orderbysku", method = RequestMethod.GET)
    public String showOrderBySku(Model model) {
        List<Product> productsOrderBySku = productRepository.findAll(Sort.by(Sort.Direction.ASC, "productSku"));
        model.addAttribute("productsOrderBySku", productsOrderBySku);
        return "searchlists/orderbysku";
    }

    /**
     * Sort by brand page
     * @param model - html handler
     * @return - orderbybrand
     */
    @RequestMapping(value = "/searchlists/orderbybrand", method = RequestMethod.GET)
    public String showOrderByBrand(Model model) {
        List<Product> productsOrderByBrand = productRepository.findAll(Sort.by(Sort.Direction.DESC, "productBrand"));
        model.addAttribute("productsOrderByBrand", productsOrderByBrand);
        return "searchlists/orderbybrand";
    }

    /**
     * Sort by id page
     * @param model - html handler
     * @return - mainlist
     */
    @RequestMapping(value = "/searchlists/mainlist", method = RequestMethod.GET)
    public String showProductsMainList(Model model) {
        List<Product> productsMainList = productRepository.findAll();
        model.addAttribute("productsMainList", productsMainList);
        return "searchlists/mainlist";
    }
}
