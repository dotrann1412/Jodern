from sentence_transformers import SentenceTransformer
from modules.retriever.Retriever import Retriever

text_model = SentenceTransformer('keepitreal/vietnamese-sbert')
print('[STATUS] Text retrieval model loaded')

class TextRetriever():
    def __init__(self, features_folder='data/res/features/texts', top_k=12, cut_off=1.2):
        '''
            Args:
                features_folder: path to folder containing text features
                top_k: maximum number of images to return
                cut_off: cut off distance
        '''
        self.__retriever = Retriever(features_folder, vector_dim=768, multiple_features=False)
        self.top_k = top_k
        self.cut_off = cut_off
    
    def search(self, query):
        '''
            Args:
                query: Vietnamese string

            Returns:
                list of ID of relavant products
        '''
        query_embedded = text_model.encode([query])
        indices = self.__retriever.search(query_embedded, top_k=self.top_k, cut_off=self.cut_off)
        return indices
