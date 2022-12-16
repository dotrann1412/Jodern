import faiss
import numpy as np
import os

class Retriever():
    def __init__(self, features_folder, vector_dim=512, multiple_features=False):
        self.index = faiss.IndexFlatIP(vector_dim)
        self.multiple_features = multiple_features
        self.__load_features(features_folder)

    def __load_features(self, features_folder):
        all_features = []
        file_ids = sorted(int(x.split('.')[0]) for x in os.listdir(features_folder))
        for id in file_ids:
            if self.multiple_features:
                features = np.load(os.path.join(features_folder, f'{id}.npy'))
                for f in features:
                    all_features.append(f)
            else:
                all_features.append(np.load(os.path.join(features_folder, f'{id}.npy')))
        all_features = np.array(all_features).astype(np.float32)
        faiss.normalize_L2(all_features)
        self.index.add(all_features)

    def search(self, X, top_k=12, cut_off=0.7):
        faiss.normalize_L2(X)
        distances, indices = self.index.search(X.astype(np.float32), top_k)
        for i in range(len(distances[0])):
            if distances[0][i] < cut_off:
                return indices[0][:i]
        return indices[0]