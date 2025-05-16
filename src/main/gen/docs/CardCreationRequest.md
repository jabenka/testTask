

# CardCreationRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**cardNumber** | **String** |  |  |
|**ownerId** | **UUID** |  |  |
|**expiresIn** | **LocalDate** |  |  |
|**status** | [**StatusEnum**](#StatusEnum) |  |  |
|**startBalance** | **BigDecimal** |  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| EXPIRED | &quot;EXPIRED&quot; |
| ACTIVE | &quot;ACTIVE&quot; |
| BLOCKED | &quot;BLOCKED&quot; |



