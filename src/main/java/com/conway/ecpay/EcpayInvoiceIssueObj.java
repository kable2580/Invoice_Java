package com.conway.ecpay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EcpayInvoiceIssueObj {
  private String customerIdentifier = "";
  private String customerId = "";
  private String customerName = "";
  private String customerAddr = "";
  private String customerPhone = "";
  private String customerEmail = "";
  private String clearanceMark = "";
  private String print = "";
  private String donation = "";
  private String loveCode = "";
  private String carruerType = "";
  private String carruerNum = "";
  private String taxType = "";
  private String salesAmount = "";
  private String invoiceRemark = "";
  private String itemName = "";
  private String itemCount = "";
  private String itemWord = "";
  private String itemPrice = "";
  private String itemTaxType = "";
  private String itemAmount = "";
  private String itemRemark = "";
  private String invType = "";
  private String vat = "";
}
