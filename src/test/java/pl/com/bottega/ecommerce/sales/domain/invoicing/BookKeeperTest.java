package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    BookKeeper bookKeeper;
    @Mock
    TaxPolicy mockTaxPolicy;


    @Before
    public void setUp() {
        mockTaxPolicy = mock(TaxPolicy.class);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        when(mockTaxPolicy.calculateTax(any(), any())).thenReturn(new Tax(new Money(BigDecimal.ONE), "Tax"));
    }

    //state tests
    @Test
    public void issuance_oneItem() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        Money money = new Money(BigDecimal.ONE);
        RequestItem requestItem = new RequestItemBuilder().setProductData(new ProductBuilder().setAggregateId(Id.generate()).setPrice(money).setName("Apple").setProductType(ProductType.FOOD).createProduct().generateSnapshot()).setQuantity(3).setTotalCost(money).createRequestItem();
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, mockTaxPolicy);
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    public void issuance_zeroItems() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, mockTaxPolicy);
        assertEquals(0, invoice.getItems().size());
    }

    @Test
    public void issuance_fiveItems() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        for (int i = 1; i <= 5; i++) {
            Money money = new Money(BigDecimal.ONE);
            RequestItem requestItem = new RequestItemBuilder().setProductData(new ProductBuilder()
                    .setAggregateId(Id.generate()).setPrice(money).setName("Item" + i).setProductType(ProductType.FOOD)
                    .createProduct().generateSnapshot()).setQuantity(new Random().nextInt(15))
                    .setTotalCost(money).createRequestItem();
            invoiceRequest.add(requestItem);
        }
        Invoice invoice = bookKeeper.issuance(invoiceRequest, mockTaxPolicy);
        assertEquals(5, invoice.getItems().size());
    }

    //behaviour tests
    @Test
    public void issuance_twoItems() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        Money money1 = new Money(BigDecimal.ONE);
        RequestItem requestItem1 = new RequestItemBuilder().setProductData(new ProductBuilder()
                .setAggregateId(Id.generate()).setPrice(money1).setName("Apple").setProductType(ProductType.FOOD)
                .createProduct().generateSnapshot()).setQuantity(3).setTotalCost(money1).createRequestItem();
        invoiceRequest.add(requestItem1);

        Money money2 = new Money(BigDecimal.TEN);
        RequestItem requestItem2 = new RequestItemBuilder().setProductData(new ProductBuilder()
                .setAggregateId(Id.generate()).setPrice(money2).setName("Apap").setProductType(ProductType.DRUG)
                .createProduct().generateSnapshot()).setQuantity(2).setTotalCost(money2).createRequestItem();
        invoiceRequest.add(requestItem2);

        bookKeeper.issuance(invoiceRequest, mockTaxPolicy);

        verify(mockTaxPolicy).calculateTax(ProductType.FOOD, money1);
        verify(mockTaxPolicy).calculateTax(ProductType.DRUG, money2);
        verify(mockTaxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));

    }

    @Test
    public void issuance_noInvocation() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);

        bookKeeper.issuance(invoiceRequest, mockTaxPolicy);
        verify(mockTaxPolicy, never()).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void issuance_moreThanThreeInvocations() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest invoiceRequest = new InvoiceRequest(clientData);
        for (int i = 1; i <= 5; i++) {
            Money money = new Money(BigDecimal.ONE);
            RequestItem requestItem = new RequestItemBuilder().setProductData(new ProductBuilder()
                    .setAggregateId(Id.generate()).setPrice(money).setName("Item" + i).setProductType(ProductType.FOOD)
                    .createProduct().generateSnapshot()).setQuantity(new Random().nextInt(15))
                    .setTotalCost(money).createRequestItem();
            invoiceRequest.add(requestItem);
        }
        bookKeeper.issuance(invoiceRequest, mockTaxPolicy);
        verify(mockTaxPolicy, atLeast(3)).calculateTax(any(ProductType.class), any(Money.class));
    }
}