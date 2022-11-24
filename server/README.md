# Server side

## General 

This folder store code for handling all of our's application services.

## Data retrievers (for searching features)
### Experiments:
- [Image retriever](https://www.kaggle.com/trngvhong/jodern-image-test-vit-b-16)
- [Text retriever](https://www.kaggle.com/code/trngvhong/jodern-texts-test-sbert/notebook)

### Usage:
- Image retriever:
```python
from retriever.ImageRetriever import ImageRetriever

retriever = ImageRetriever(top_k=10)

image_path = '...'
print(retriever.search(image_path))

image_url = '...'
print(retriever.search(image_url))

image_data = '...' # numpy array
print(retriever.search(image_data))
```

- Text retriever:
```python
from retriever.TextRetriever import TextRetriever

retriever = TextRetriever(top_k=10)

query = 'chân váy phong cách'
print(retriever.search(query))