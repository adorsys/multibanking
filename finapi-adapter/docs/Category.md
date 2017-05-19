
# Category

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Category identifier | 
**name** | **String** | Category name | 
**parentId** | **Long** | Identifier of the parent category (if a parent category exists) |  [optional]
**parentName** | **String** | Name of the parent category (if a parent category exists) |  [optional]
**isCustom** | **Boolean** | Whether the category is a finAPI global category (in which case this field will be false), or the category was created by a user (in which case this field will be true) | 
**children** | **List&lt;Long&gt;** | List of sub-categories identifiers (if any exist) |  [optional]



