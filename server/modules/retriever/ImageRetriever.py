import numpy as np
import requests
from PIL import Image
import torch
from transformers import AutoFeatureExtractor, Swinv2Model
from io import BytesIO

from modules.retriever.Retriever import Retriever

class MySwinT2(torch.nn.Module):
    def __init__(self):
        super().__init__()
        self.feature_extractor = AutoFeatureExtractor.from_pretrained("microsoft/swinv2-tiny-patch4-window8-256")
        self.model = Swinv2Model.from_pretrained("microsoft/swinv2-tiny-patch4-window8-256")
        self.model.eval()
        
    def forward(self, image):
        # x: an image loaded by PIL.Image.Open
        inputs = self.feature_extractor(image, return_tensors="pt")
        with torch.no_grad():
            outputs = self.model(**inputs)
        return outputs.pooler_output[0].numpy()
    
model = MySwinT2()
image_indices = np.load('data/res/features/images/image_indices.npy')
print('[STATUS] Image retrieval model loaded')

def image_preprocess(data):
    if isinstance(data, np.ndarray):
        img = Image.fromarray(data)
    elif data.find('https://') != -1:
        response = requests.get(data)
        img = Image.open(BytesIO(response.content))
    else:
        img = Image.open(data)
    if np.asarray(img).shape[-1] > 3:
        img = Image.fromarray(np.asarray(img)[:,:,:3])
    return img


class ImageRetriever():
    def __init__(self, features_folder='data/res/features/images/values', top_k=16, cut_off=0.5):
        '''
            Args:
                features_folder: path to folder containing image features
                cut_off: cut off distance
                top_k: maximum number of returned products
        '''
        self.__retriever = Retriever(features_folder, vector_dim=768, multiple_features=True)
        self.cut_off = cut_off
        self.top_k = top_k
    
    def search(self, query):
        '''
            Args:
                query: numpy array with shape H * W * C or a string (path to image file or url)

            Returns:
                list of ID of relavant products
        '''
        img = image_preprocess(query)
        with torch.no_grad():
            features = np.array([model(img)])
        indices = self.__retriever.search(features, top_k=self.top_k, cut_off=self.cut_off)
        res = []
        for i in indices:
            if image_indices[i] in res:
                continue
            res.append(image_indices[i])
        return res