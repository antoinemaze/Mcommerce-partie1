package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api( description="API pour es opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")

    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }


    //ajouter un produit
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
        /*
        Partie 3 - Validation du prix de vente

        Si le prix de vente est de 0, lancez une exception du nom de  ProduitGratuitException
        qui retournera le bon code HTTP pour ce cas avec un message explicatif que vous définirez.
         */
        if(product.getPrix() == 0)
            throw new ProduitGratuitException("Le produit saisi a un prix de vente de 0.");

        Product productAdded =  productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }

    /*
    Partie 1 - Affichage de la marge
    La méthode calculerMargeProduit doit répondre à  une requête GET
    sur l’URI   /AdminProduits. Les données doivent être récupérées
    depuis la base de données mises en place dans le projet.
 */
    @ApiOperation(value = "Affichage de la marge sur le produit")
    @GetMapping(value = "/AdminProduits")
    public Map<String, String>   calculerMargeProduit() {

        Map<String, String> results = new HashMap<String, String>();
        for (Product product : productDao.findAll()) {
            results.put(product.toString(), Integer.toString(product.getPrix()-product.getPrixAchat()));
        }
        return results;
    }

    /*
    Partie 2 - Tri par ordre alphabétique
    La méthode trierProduitsParOrdreAlphabetique trie les Produits Par Ordre Alphabetique
    et doit impérativement faire appel
    à une méthode que vous allez ajouter dans ProductDao  qui utilise le nommage
    conventionné de Spring Data JPA pour générer automatiquement les requêtes
 */
    @ApiOperation(value = "Tri par ordre alphabétique")
    @GetMapping(value = "/TrierProduits")
    public List<Product>  trierProduitsParOrdreAlphabetique() {
        return productDao.findAllByOrderByNomAsc();
    }


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }



}
