from sentence_transformers import SentenceTransformer
import numpy as np

from modules.retriever.Retriever import Retriever

text_model = SentenceTransformer('keepitreal/vietnamese-sbert')
text_indices = np.load('data/res/features/texts/text_indices.npy')
print('[STATUS] Text retrieval model loaded')

class TextRetriever():
    def __init__(self, features_folder='data/res/features/texts/values', top_k=16, cut_off=0.4):
        '''
            Args:
                features_folder: path to folder containing text features
                cut_off: cut off distance (cosine similarity)
                top_k: maximum number of returned products
        '''
        self.__retriever = Retriever(features_folder, vector_dim=768, multiple_features=True)
        self.cut_off = cut_off
        self.top_k = top_k
    
    def search(self, query):
        '''
            Args:
                query: Vietnamese string

            Returns:
                list of ID of relavant products
        '''
        query_embedded = text_model.encode([query])
        indices = self.__retriever.search(query_embedded, top_k=self.top_k, cut_off=self.cut_off)
        res = []
        for i in indices:
            if text_indices[i] in res:
                continue
            res.append(text_indices[i])
        return res