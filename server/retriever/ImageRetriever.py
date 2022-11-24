import numpy as np
import requests
from PIL import Image
import torch
import clip
import cv2
from io import BytesIO

from retriever.Retriever import Retriever

class MyViTModel(torch.nn.Module):
    def __init__(self, name="ViT-B/16", device="cpu"):
        super().__init__()
        self.model, self.preprocess = clip.load(name, device=device)
        self.model.eval()

    def forward(self, x):
        x = self.preprocess(x).unsqueeze(0)
        with torch.no_grad():
            x = self.model.encode_image(x)
        return x
    

model = MyViTModel()
images_idx = np.load('data/features/images/image_indices.npy')


def resize(inputs, size=224):
    '''
        inputs: numpy array with shape H * W * C

        resize to new_h * new_w * c with keeping the aspect ratio (mininum is size)
    '''
    h, w, c = inputs.shape
    if h < w:
        new_h = size
        new_w = int(w * size / h)
    else:
        new_w = size
        new_h = int(h * size / w)
    inputs = cv2.resize(inputs, (new_w, new_h))
    return inputs


def image_preprocess(data):
    if isinstance(data, np.ndarray):
        img = Image.fromarray(data)
    elif data.find('https://') != -1:
        response = requests.get(data)
        img = Image.open(BytesIO(response.content))
    else:
        img = Image.open(data)
    return img


class ImageRetriever():
    def __init__(self, features_folder='data/features/images/values', top_k=12, cut_off=1.0):
        '''
            Args:
                features_folder: path to folder containing image features
                top_k: maximum number of images to return
                cut_off: cut off distance
        '''
        self.__retriever = Retriever(features_folder, vector_dim=512, multiple_features=True)
        self.top_k = top_k
        self.cut_off = cut_off
    
    def search(self, query):
        '''
            Args:
                query: numpy array with shape H * W * C or a string (path to image file or url)

            Returns:
                list of ID of relavant products
        '''
        img = image_preprocess(query)
        with torch.no_grad():
            features = model(img)
        features = features.cpu().numpy()
        indices = self.__retriever.search(features, top_k=self.top_k, cut_off=self.cut_off)
        res = []
        for i in indices:
            if images_idx[i] in res:
                continue
            res.append(images_idx[i])
        return res