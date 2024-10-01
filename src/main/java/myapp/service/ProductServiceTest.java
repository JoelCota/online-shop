package myapp.service;

import myapp.domain.Product;
import myapp.domain.enumeration.ProductStatus;
import myapp.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private final Validator validator;

    public ProductServiceTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // 1. Prueba de creación de producto válido
    @Test
    void testCreateValidProduct() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());

        when(productRepository.save(product)).thenReturn(product);

        // Act
        Product createdProduct = productService.save(product);

        // Assert
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getTitle()).isEqualTo("Producto Válido");
        assertThat(createdProduct.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    // 2. Prueba con título muy corto
    @Test
    void testCreateProductWithShortTitle() {
        // Arrange
        Product product = new Product();
        product.setTitle("ab"); // Título inválido (demasiado corto)
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("title") &&
            v.getMessage().contains("size must be between 3 and 100")
        );
    }

    // 3. Prueba con título muy largo
    @Test
    void testCreateProductWithLongTitle() {
        // Arrange
        Product product = new Product();
        product.setTitle("A".repeat(101)); // Título inválido (demasiado largo)
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("title") &&
            v.getMessage().contains("size must be between 3 and 100")
        );
    }

    // 4. Prueba con precio negativo
    @Test
    void testCreateProductWithNegativePrice() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("-1.00")); // Precio inválido
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("price") &&
            v.getMessage().contains("must be greater than or equal to 0")
        );
    }

    // 5. Prueba con cantidad en stock negativa
    @Test
    void testCreateProductWithNegativeQuantityInStock() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setQuantityInStock(-1); // Cantidad inválida
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("quantityInStock") &&
            v.getMessage().contains("must be greater than or equal to 0")
        );
    }

    // 6. Prueba con estado inválido
    @Test
    void testCreateProductWithInvalidStatus() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(null); // Estado inválido
        product.setDateAdded(Instant.now());

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("status") &&
            v.getMessage().contains("must not be null")
        );
    }

    // 7. Prueba con fecha de adición nula
    @Test
    void testCreateProductWithNullDateAdded() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(null); // Fecha inválida

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("dateAdded") &&
            v.getMessage().contains("must not be null")
        );
    }

    // 8. Prueba con descripción muy corta
    @Test
    void testCreateProductWithShortDescription() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());
        product.setDescription("Corto"); // Menos de 10 caracteres

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("description") &&
            v.getMessage().contains("size must be between 10 and 1000")
        );
    }

    // 9. Prueba con palabras clave que exceden el límite
    @Test
    void testCreateProductWithExceedingKeywordsLength() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());
        product.setKeywords("A".repeat(201)); // Excede 200 caracteres

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("keywords") &&
            v.getMessage().contains("size must be between")
        );
    }

    // 10. Prueba con peso negativo
    @Test
    void testCreateProductWithNegativeWeight() {
        // Arrange
        Product product = new Product();
        product.setTitle("Producto Válido");
        product.setPrice(new BigDecimal("99.99"));
        product.setStatus(ProductStatus.IN_STOCK);
        product.setDateAdded(Instant.now());
        product.setWeight(-5.0); // Peso inválido

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("weight") &&
            v.getMessage().contains("must be greater than or equal to 0")
        );
    }
}
