from modules.data_acess.driver import Connector
from modules.helpers.config import Config
import platform

try:
    import pyjwt
except:
    import jwt as pyjwt

import datetime

class Verifier:
    def decode(token):
        return pyjwt.decode(token, Config.getValue('app-secret-key'), algorithms=['HS256'])

    def safeDecode(token):
        try: return Verifier.decode(token)
        except: pass
        return {}

    def verify(**kwargs):
        try: 
            pyjwt.decode(kwargs['token'], Config.getValue('app-secret-key'), algorithms=['HS256'])
        except: return False
        return True

    def generateToken(**kwargs):
        return pyjwt.encode(kwargs['payload'], Config.getValue('app-secret-key'), algorithm='HS256')        

class Authenticator:
    def Login(**kwargs):
        userid = kwargs.get("userid", "Unknown")
        fullname = kwargs.get("fullname", "Unknown")
        email = kwargs.get("email", "Unknown")
        phone = kwargs.get("phone", "Unknown")
        token = kwargs.get("token", "Unknown")

        if not Authenticator.verify(userid, token):
            return {
                "error": "invalid token"
            }
        
        cursor = Connector.establishConnection().cursor()
        query = "select * from users where userid = ?"
        cursor.execute(query, userid)
        row = cursor.fetchone()
        if not row:
            query = "insert into users (userid, fullname, email, Phone, Address) values (?, ?, ?, ?, ?)"
            cursor.execute(query, (userid, fullname, email, phone, ""))
            cursor.commit()

        return {
            "message": "Chào mừng đến với Jodern!",
            "access_token": Verifier.generateToken (
                payload = {
                    "userid": userid,
                    "email": email,
                    "phone": phone
                }
            )
        }

    #@todo: implement this
    def verify(userid, token):
        return True