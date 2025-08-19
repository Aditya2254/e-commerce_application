# Product Images Feature Documentation

This document provides information on how to use the product images feature in the e-commerce application.

## Overview

The product images feature allows you to:
- Upload one or more images for a product
- Retrieve images associated with a product
- Delete images from a product
- Set a specific image as the primary image for a product
- Retrieve products with their associated images

## API Endpoints

### Product Image Operations

#### Get Product Images

```
GET /api/products/{productId}/images
```

Retrieves all images for a specific product.

**Response:**
```json
[
  {
    "id": 1,
    "url": "https://localhost:9000/product-images/products/1/image1.jpg",
    "primary": true,
    "orderIndex": 0,
    "contentType": "image/jpeg",
    "sizeBytes": 12345,
    "widthPx": 800,
    "heightPx": 600
  },
  {
    "id": 2,
    "url": "https://localhost:9000/product-images/products/1/image2.jpg",
    "primary": false,
    "orderIndex": 1,
    "contentType": "image/jpeg",
    "sizeBytes": 23456,
    "widthPx": 800,
    "heightPx": 600
  }
]
```

#### Upload Product Images

```
POST /api/products/{productId}/images
Content-Type: multipart/form-data
```

Uploads one or more images for a specific product.

**Request:**
- Form data with one or more files under the key "files"

**Response:**
```json
{
  "productId": 1,
  "images": [
    {
      "id": 3,
      "url": "https://localhost:9000/product-images/products/1/image3.jpg",
      "primary": false,
      "orderIndex": 2,
      "contentType": "image/jpeg",
      "sizeBytes": 34567,
      "widthPx": 800,
      "heightPx": 600
    }
  ]
}
```

#### Delete Product Image

```
DELETE /api/products/{productId}/images/{imageId}
```

Deletes a specific image from a product.

**Response:**
```json
{
  "message": "Image deleted successfully"
}
```

#### Set Primary Image

```
PUT /api/products/{productId}/images/{imageId}/primary
```

Sets a specific image as the primary image for a product.

**Response:**
```json
{
  "id": 2,
  "url": "https://localhost:9000/product-images/products/1/image2.jpg",
  "primary": true,
  "orderIndex": 1,
  "contentType": "image/jpeg",
  "sizeBytes": 23456,
  "widthPx": 800,
  "heightPx": 600
}
```

### Product Operations with Images

#### Get All Products with Images

```
GET /products/with-images
```

Retrieves all products with their associated images.

**Response:**
```json
{
  "products": [
    {
      "id": 1,
      "name": "Product 1",
      "description": "Description 1",
      "price": 10.99,
      "stock": 100,
      "category": "Category 1"
    },
    {
      "id": 2,
      "name": "Product 2",
      "description": "Description 2",
      "price": 20.99,
      "stock": 200,
      "category": "Category 2"
    }
  ],
  "images": {
    "1": [
      {
        "id": 1,
        "url": "https://localhost:9000/product-images/products/1/image1.jpg",
        "primary": true,
        "orderIndex": 0,
        "contentType": "image/jpeg",
        "sizeBytes": 12345,
        "widthPx": 800,
        "heightPx": 600
      }
    ],
    "2": [
      {
        "id": 2,
        "url": "https://localhost:9000/product-images/products/2/image2.jpg",
        "primary": true,
        "orderIndex": 0,
        "contentType": "image/jpeg",
        "sizeBytes": 23456,
        "widthPx": 800,
        "heightPx": 600
      }
    ]
  }
}
```

#### Get Product with Images

```
GET /products/{id}/with-images
```

Retrieves a specific product with its associated images.

**Response:**
```json
{
  "product": {
    "id": 1,
    "name": "Product 1",
    "description": "Description 1",
    "price": 10.99,
    "stock": 100,
    "category": "Category 1"
  },
  "images": [
    {
      "id": 1,
      "url": "https://localhost:9000/product-images/products/1/image1.jpg",
      "primary": true,
      "orderIndex": 0,
      "contentType": "image/jpeg",
      "sizeBytes": 12345,
      "widthPx": 800,
      "heightPx": 600
    }
  ]
}
```

## Usage Examples

### Uploading Images for a Product

```javascript
// Using fetch API
const formData = new FormData();
formData.append('files', file1);
formData.append('files', file2);

fetch('/api/products/1/images', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Displaying a Product with its Images

```javascript
// Using fetch API
fetch('/products/1/with-images')
.then(response => response.json())
.then(data => {
  // Display product details
  const product = data.product;
  document.getElementById('product-name').textContent = product.name;
  document.getElementById('product-description').textContent = product.description;
  document.getElementById('product-price').textContent = `$${product.price}`;
  
  // Display product images
  const imagesContainer = document.getElementById('product-images');
  imagesContainer.innerHTML = '';
  
  // Find primary image to display first
  const primaryImage = data.images.find(img => img.primary);
  if (primaryImage) {
    const imgElement = document.createElement('img');
    imgElement.src = primaryImage.url;
    imgElement.classList.add('primary-image');
    imagesContainer.appendChild(imgElement);
  }
  
  // Display other images
  data.images.filter(img => !img.primary).forEach(img => {
    const imgElement = document.createElement('img');
    imgElement.src = img.url;
    imgElement.classList.add('secondary-image');
    imagesContainer.appendChild(imgElement);
  });
})
.catch(error => console.error('Error:', error));
```

## Configuration

The product images feature uses AWS S3 or compatible storage (like MinIO) for storing images. The configuration is defined in `application.properties`:

```properties
# App image storage configuration
app.images.s3.bucket=product-images
app.images.s3.region=ap-south-1
app.images.s3.endpoint=http://minio.minio:9000
app.images.s3.path-style-access=true
app.images.s3.public-base-url=https://localhost:9000/product-images/
app.images.s3.signed-url-ttl=900s

# Spring file upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

AWS credentials should be provided via environment variables:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`