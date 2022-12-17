import json, os


class Config:
    __confFile = './config/config.json'
    __conf = {}
    __loaded = False
    
    def __loadConfig():
        if not os.path.exists(Config.__confFile):
            return

        with open(Config.__confFile, 'r') as fp:
            Config.__conf = json.load(fp)

        Config.__loaded = True
    
    def getValue(key, default = None):
        if not Config.__loaded:
            Config.__loadConfig()

        if key in Config.__conf:
            return Config.__conf[key]
        return default
    
    def setValue(key, value, commitment = False):
        if not Config.__loaded:
            Config.__loadConfig()
        Config.__conf[key] = value
        if commitment:
            with open(Config.__confFile, 'w') as fp:
                json.dump(Config.__conf, fp, indent = 4)
    
    
    