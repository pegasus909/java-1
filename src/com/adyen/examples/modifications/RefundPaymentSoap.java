package com.adyen.examples.modifications;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.common.Amount;
import com.adyen.services.payment.ModificationRequest;
import com.adyen.services.payment.ModificationResult;
import com.adyen.services.payment.Payment;
import com.adyen.services.payment.PaymentPortType;
import com.adyen.services.payment.ServiceException;

/**
 * Refund a Payment (SOAP)
 * 
 * Settled payments can be refunded by sending a modifiction request to the refund action of the WSDL. This file shows
 * how a settled payment can be refunded by a modification request using SOAP.
 * 
 * Please note: using our API requires a web service user. Set up your Webservice user:
 * Adyen CA >> Settings >> Users >> ws@Company. >> Generate Password >> Submit
 * 
 * @link /4.Modifications/Soap/RefundPayment
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet("/4.Modifications/Soap/RefundPayment")
public class RefundPaymentSoap extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/Payment.wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 * 
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		Payment service = new Payment(new URL(wsdl));
		PaymentPortType client = service.getPaymentHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * Perform refund request by sending in a modificationRequest, the protocol is defined in the WSDL.
		 * The following parameters are used:
		 * 
		 * <pre>
		 * - merchantAccount: the merchant account the payment was processed with
		 * - modificationAmount: the amount to refund
		 *     - currency: the currency must match the original payment
		 *     - amount: the value must be the same or less than the original amount
		 * - originalReference: the pspReference that was assigned to the authorisation
		 * - reference: your own reference or description to the modification (optional)
		 * </pre>
		 */
		ModificationRequest modificationRequest = new ModificationRequest();
		modificationRequest.setMerchantAccount("YourMerchantAccount");
		modificationRequest.setOriginalReference("PspReferenceOfTheAuthorisedPayment");
		modificationRequest.setReference("YourReference");

		Amount amount = new Amount();
		amount.setCurrency("EUR");
		amount.setValue(199L);
		modificationRequest.setModificationAmount(amount);

		/**
		 * Send the refund request.
		 */
		ModificationResult modificationResult;
		try {
			modificationResult = client.refund(modificationRequest);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}

		/**
		 * If the message was syntactically valid and merchantAccount is correct you will receive a modification
		 * response with the following fields:
		 * - pspReference: A new reference to uniquely identify this modification request.
		 * - response: A confirmation indicating we receievd the request: [refund-received].
		 * 
		 * Please note: The result of the refund is sent via a notification with eventCode REFUND.
		 */
		PrintWriter out = response.getWriter();

		out.println("Modification Result:");
		out.println("- pspReference: " + modificationResult.getPspReference());
		out.println("- response: " + modificationResult.getResponse());
	}

}
