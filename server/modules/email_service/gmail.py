from smtplib import SMTP_SSL, SMTP_SSL_PORT
import imaplib
import email
import email.message
import mimetypes

SMTP_HOST = 'smtp.gmail.com'
IMAP_HOST = 'imap.gmail.com'

APP_REQ = '[LTR]'

def html_msg(msg, status = None, bold_all=False):
    content = msg
    _class = ''

    if bold_all:
        _class += 'bold '
    if status is not None:
        if not status:
            content += ' Please try again later.'
        _class += ('ok' if status else 'error')

    html = f'<p lang="en" class="message {_class}">{content}</p>'
    return html

def get_item_html_template(image, pName, price, itemCount, size):
    return '''<div class="item-block">
        <img src="{image}" class="item-image" alt="image" />
        <div class="item-info">
            <div class="item-title"><p><a href="{image}">
                <b>{productName}</b></a></p></div>
            <div class="item-detail">
                <div><p><b>Đơn giá: </b> {price:,.3f}</p></div>
                <div><p><b>Số lượng: </b> {itemCount}</p></div>
                <div><p><b>Size: </b> {size}</p></div>
            </div>
        </div>
    </div>
    '''.format (
        image = image,
        productName = pName,
        price = price,
        itemCount = itemCount,
        size = size
    )

from datetime import datetime
import datetime

def html_mail(**kwargs):
    total = kwargs.get('price', 0) + kwargs.get('shipping_fee', 0)
    
    preoder_required = kwargs['preorder_required']

    delivered_on = datetime.datetime.now() + datetime.timedelta(days = 5)
    # if preoder_required:
    #     delivered_on += datetime.timedelta(days = 2)

    html_template = str('''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <style>
        * {{
            box-sizing: border-box;
        }}
        
        html {{
            font-family: 'Roboto', sans-serif;
        }}
        
        p, td, th, span, ul {{
            color: #333;
            font-size: 16px;
        }}
        .main {{
            margin: 0 auto;
            border: 1px solid #ccc;
            border-radius: 10px;
            padding: 6px 30px 30px 30px;
            width: 700px;
        }}
        .app__name {{
            text-align: center;
            font-size: 24px;
            color: #1e9d95;
            font-weight: bold;
        }}
        .app__greeting,
        .app__desc {{
            text-align: center;
        }}
        .divider {{
            border-bottom: 1px solid #ccc;
            margin: 20px 0;
        }}
        .request {{
            font-weight: bold;
            word-break: break-all;
        }}
        /* CSS for tables */
        table {{
            /* width: 100%; */
            margin: 0 auto;
            border-collapse: collapse;
            overflow: hidden;
        }}
        table td, table th {{
            font-size: 14px;
        }}
        table.left {{
            text-align: left;
        }}
        table.center {{
            text-align: center;
        }}
        td,
        th {{
            border-top: 1px solid #c6cccde6;
            padding: 10px 14px;
        }}
        th {{
            background-color: #76dfd8;
            border-left: 1px solid #c6cbcd;
            border-right: 1px solid #c6cbcd;
        }}
        td {{
            border-left: 1px solid #c6cbcd;
            border-right: 1px solid #c6cbcd;
        }}
        
        tr.first-row {{
            text-align: center;
        }}
        tr.last-row {{
            border-bottom: 1px solid #c6cccde6;
        }}
        tr.odd-row td {{
            background-color: #e6f8f7;
        }}
        /* CSS for message */
        .message {{
            margin: 0;
        }}
        .message.bold {{
            font-weight: bold;
        }}
        .message.ok {{
            color: #1e9d95;
        }}
        .message.error {{
            color: red;
        }}
        .ascii {{
            font-family: 'Courier New', monospace;
            font-size: 16px;
            margin: 0;
            margin-left: 40px;
            white-space: pre-wrap;
            font-weight: bold;
        }}
        .item-block {{
            width: 100%;
            min-height: 110px;
            border-radius: 1rem;
            border: 0.2px solid rgba(80, 80, 80, 1);
            background-color: #f8fcfc;
            margin: 5px;
            padding: 5px;   
            align-items: center;
            display: inline-flex;
        }}
        
        .item-name {{
            font-weight: bold;
            margin-left: 10px;
        }}

        .item-image {{
            max-width: 200px;
            max-height: 120px;
            margin: auto;
            border-radius: 1rem;
            border: none;
            padding: 10px;
            margin-left: 0px;
        }}
            
        .order-summary {{
            text-align: right;
            position: relative;
            padding-right: 20px;
            margin: 15px;
        }}

        .item-info {{
            height: 90%;
            width: 100%;
            display: block;
        }}

        .item-title {{
            max-width: 100%;
            display: block;
            margin-left: 10%;
            text-align: left;
            font-size: 24px;
        }}

        .item-title a:hover {{
            text-decoration: underline;
            text-decoration-color: #76dfd8;
        }}

        .item-title a:link {{
            text-decoration: none;
        }}

        .item-title a {{
            color: #1e9d95;
        }}

        .item-detail {{
            width: 100%;
            display: inline-flex;
            direction: rtl;
            margin-right: 10px;
        }}

        .item-detail div {{
            margin-right: 15px
        }}
    </style>
</head>
<body>
    <div class='main'>
        <div class="container">
            <p class="app__name">JODERN STORE</p>
            <div class='divider'></div>
        </div>

        <div class="container">
            <p>Jodern Store xin cảm ơn quý khách hàng <b>{customer_name}</b> vì đã tin tưởng và sử dụng dịch vụ của chúng tôi. Đơn hàng của quý khách bao gồm <b>{pcnt}</b> sản phẩm, chi tiết như sau:</p>
        </div>

        <div class="container">
            {content}
        </div>

        <div class="container order-summary">
            <p><b>Sản phẩm: </b>{price:,.3f} (VND)</p>
            <p><b>Phí vận chuyển:</b> {shipping_fee:,.3f} (VND)</p>
            <hr width="50%" style="margin-right: 0"/>
            <p><b>Tổng:</b> {total:,.3f} (VND)</p>
        </div>

        <div class="container">
            <p>Đơn hàng của quý khách đã được xác nhận và sẽ được giao trước ngày <b>{day}</b> đến địa chỉ <b>{address}</b> và liên lạc qua số điện thoại <b>{phone}</b>.</p>
            {more}
        </div>
    </div>
    </body>
</html>
    ''').format(
        total = total,
        day = delivered_on.strftime(r'%Y-%m-%d'),
        price = kwargs.get('price', 0),
        tax = kwargs.get('tax', 0),
        shipping_fee = kwargs.get('shipping_fee', 0),
        content = kwargs.get('html_content'),
        pcnt = kwargs.get('product_count', 0),
        customer_name = kwargs.get('customer_name', ''),
        phone = kwargs['phone_number'],
        address = kwargs['location'],
        more = '<p><i>(Một hoặc vài sản phẩm trong đơn hàng của quý khách cần thời gian preoder, thời gian giao có thể chậm hơn 1 hoặc 2 ngày. Quý khách hàng thông cảm cho Jodern nhé!)</i></p>' if preoder_required else ''
    )
        
    return html_template

def build_email_content(mail_from, mail_to, subject, content, format = 'html'):
    body = content['html']
    
    if 'data' in content:
        data = content['data']
    else: data = None

    email_message = email.message.EmailMessage()
    
    email_message.add_header('To', ','.join(mail_to))
    email_message.add_header('From', mail_from)
    email_message.add_header('Subject', subject)
    email_message.add_header('X-Priority', '1')
    email_message.set_content(body, format)

    if data is not None:
        maintype, _, subtype = (mimetypes.guess_type(data)[0] or 'application/octet-stream').partition("/")
        with open(data, 'rb') as fp:
            email_message.add_attachment(fp.read(), maintype = maintype, subtype = subtype, filename='log.txt')
            
    return email_message

class MailService:
    def __init__(self):
        self.imap_server = imaplib.IMAP4_SSL(IMAP_HOST)
        self.smtp_server = SMTP_SSL(SMTP_HOST, port = SMTP_SSL_PORT)

    def login(self, username, password):        
        try:
            self.imap_server.login(username, password)
            self.smtp_server.login(username, password)
        except: return False
        return True

    def logout(self):
        self.imap_server.logout()
        self.smtp_server.quit()

    def read_email (self, category = 'primary', box = 'inbox'):  
        mail_list = []
        
        try:
            self.imap_server.select(box)

            status, mail_ids = self.imap_server.search(None, f'X-GM-RAW "category:{category} in:unread"')
            
            id_list = mail_ids[0].split()
            if len(id_list) == 0:
                print('All mails are read')
                return []
            
            first_email_id = int(id_list[0])
            latest_email_id = int(id_list[-1])

            for i in range(latest_email_id, first_email_id - 1, -1):
                # 'data' will be [(header, content), b')']
                status, data = self.imap_server.fetch(str(i), '(RFC822)')
                
                mail = email.message_from_bytes(data[0][1])
                date = email.utils.parsedate_to_datetime(mail['date'])
                
                sender = email.utils.parseaddr(mail['from'])[1]
                subject = mail['subject']
                
                if not subject.upper().startswith(APP_REQ):
                    continue
                
                # Decode subject:
                subject, encoding = email.header.decode_header(subject)[0]
                if encoding:
                    subject = str(subject, encoding)


                mail_list.append({
                    'sender': sender, 
                    'subject': subject,
                })

        except Exception as e:
            print('Error while reading the mail inbox as below:')
            print(str(e))
            
        return mail_list

    def send_mail(self, mail):
        try: self.smtp_server.sendmail(mail['From'], mail['To'], str(mail).encode())
        except Exception as e:
            print(str(e))
            return False
        return True
