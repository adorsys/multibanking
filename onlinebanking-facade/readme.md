# Onlinebanking Facade

## Introduction

The Onlinebanking Facade is an adapter, that gets wrapped around standardized banking-interfaces like HBCI, Figo or 
FinAPI to normalize their methods in a form the multibanking project can work with.

Besides the interace itself, the facade also defines internal classes in which the requests from external APIs are 
getting mapped into.
