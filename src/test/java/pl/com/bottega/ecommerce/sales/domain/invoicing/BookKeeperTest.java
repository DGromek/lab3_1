package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mockito.Mock;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTest {
    BookKeeper bookKeeper;
    InvoiceRequest invoiceRequest;
    Product product;

    @Mock
    TaxPolicy taxMock;

    @Before
    public void setUp() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "test"));
        product = new ProductBuilder().setAggregateId(Id.generate()).setPrice(new Money(BigDecimal.ONE)).setName("testProduct").setProductType(ProductType.DRUG).createProduct();
        taxMock = mock(TaxPolicy.class);
        when(taxMock.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(BigDecimal.ONE), "tax"));
    }

    @Test
    public void issuance_Req1Element() {
        //given
        invoiceRequest.add(new RequestItemBuilder().setProductData(product.generateSnapshot()).setQuantity(45).setTotalCost(new Money(BigDecimal.ONE)).createRequestItem());

        //when
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        //then
        assertThat(invoice.getItems(), hasSize(1));
    }

    @Test
    public void issuance_Req0Elements() {
        //when
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        //then
        assertThat(invoice.getItems(), hasSize(0));
    }

    @Test
    public void issuance_ReqMoreThan1Element() {
        int howManyElements = 13;
        //given
        for (int i = 0; i < howManyElements; i++) {
            invoiceRequest.add(new RequestItemBuilder().setProductData(product.generateSnapshot()).setQuantity(45).setTotalCost(new Money(BigDecimal.ONE)).createRequestItem());
        }

        //when
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        //then
        assertThat(invoice.getItems(), hasSize(howManyElements));
    }


    @Test
    public void issuance_2Invocations() {
        //given
        RequestItem requestItem1 = new RequestItemBuilder().setProductData(product.generateSnapshot()).setQuantity(45).setTotalCost(new Money(BigDecimal.ONE)).createRequestItem();
        RequestItem requestItem2 = new RequestItemBuilder().setProductData(product.generateSnapshot()).setQuantity(12).setTotalCost(new Money(BigDecimal.TEN)).createRequestItem();
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        //when
        bookKeeper.issuance(invoiceRequest, taxMock);

        //then
        verify(taxMock).calculateTax(requestItem1.getProductData().getType(), requestItem1.getProductData().getPrice());
        verify(taxMock).calculateTax(requestItem2.getProductData().getType(), requestItem2.getProductData().getPrice());
        verify(taxMock, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void issuance_0Invocations() {
        //when
        bookKeeper.issuance(invoiceRequest, taxMock);

        //then
        verify(taxMock, never()).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test
    public void issuance_1Invocation() {
        //given
        RequestItem requestItem1 = new RequestItemBuilder().setProductData(product.generateSnapshot()).setQuantity(45).setTotalCost(new Money(BigDecimal.ONE)).createRequestItem();
        invoiceRequest.add(requestItem1);

        //when
        bookKeeper.issuance(invoiceRequest, taxMock);

        //then
        verify(taxMock).calculateTax(requestItem1.getProductData().getType(), requestItem1.getProductData().getPrice());
        verify(taxMock, times(1)).calculateTax(any(ProductType.class), any(Money.class));
    }
}