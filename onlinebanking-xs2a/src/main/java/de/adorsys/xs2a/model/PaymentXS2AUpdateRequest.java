/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.xs2a.model;

import java.util.Objects;

public class PaymentXS2AUpdateRequest extends XS2AUpdateRequest {
    private String paymentId;
    private String service;
    private String product;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PaymentXS2AUpdateRequest that = (PaymentXS2AUpdateRequest) o;
        return Objects.equals(paymentId, that.paymentId) &&
                       Objects.equals(service, that.service) &&
                       Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentId, service, product);
    }

    @Override
    public String toString() {
        return "PaymentXS2AUpdateRequest{" +
                       "paymentId='" + paymentId + '\'' +
                       ", service='" + service + '\'' +
                       ", product='" + product + '\'' +
                       '}';
    }
}
