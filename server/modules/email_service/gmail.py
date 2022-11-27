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

def html_mail(request, content):
    html_template = '''
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8" />
        <style>
            * {
                box-sizing: border-box;
            }
            html {
                font-family: 'Roboto', sans-serif;
            }
            
            p, td, th, span, ul {
                color: #333;
                font-size: 16px;
            }
            .main {
                margin: 0 auto;
                border: 1px solid #ccc;
                border-radius: 10px;
                padding: 6px 30px 30px 30px;
                width: 700px;
            }
            .app__name {
                text-align: center;
                font-size: 24px;
                color: #1e9d95;
                font-weight: bold;
            }
            .app__greeting,
            .app__desc {
                text-align: center;
            }
            .divider {
                border-bottom: 1px solid #ccc;
                margin: 20px 0;
            }
            .request {
                font-weight: bold;
                word-break: break-all;
            }
            /* CSS for tables */
            table {
                /* width: 100%; */
                margin: 0 auto;
                border-collapse: collapse;
                overflow: hidden;
            }
            table td, table th {
                font-size: 14px;
            }
            table.left {
                text-align: left;
            }
            table.center {
                text-align: center;
            }
            td,
            th {
                border-top: 1px solid #c6cccde6;
                padding: 10px 14px;
            }
            th {
                background-color: #76dfd8;
                border-left: 1px solid #c6cbcd;
                border-right: 1px solid #c6cbcd;
            }
            td {
                border-left: 1px solid #c6cbcd;
                border-right: 1px solid #c6cbcd;
            }
            
            tr.first-row {
                text-align: center;
            }
            tr.last-row {
                border-bottom: 1px solid #c6cccde6;
            }
            tr.odd-row td {
                background-color: #e6f8f7;
            }
            /* CSS for message */
            .message {
                margin: 0;
            }
            .message.bold {
                font-weight: bold;
            }
            .message.ok {
                color: #1e9d95;
            }
            .message.error {
                color: red;
            }
            .ascii {
                font-family: 'Courier New', monospace;
                font-size: 16px;
                margin: 0;
                margin-left: 40px;
                white-space: pre-wrap;
                font-weight: bold;
            }
        </style>
    </head>
    <body>
        <div class='main'>
            <div class="container">
                <p class="app__name">LONG TASK RUNNER</p>
                <div class='divider'></div>
            </div>
            <div class="container">
                <p>This mail responses to the result of process: <span class="request" lang="en">''' + request + '''</span></p>
            </div>
    '''
    
    '''
    <p class="app__name">Jodern Store</p>
    <p class="app__greeting">Greeting from <span style='font-weight: bold;'>Group 8</span> - Honors Program 2020, University of Science, VNUHCM.</p>
    <p class="app__desc">Thanks for chosing our service! Your order is being processed...</p>      
    '''

    html_template += f'''
        <section>
            {content}
        </section>
        </div>
        </body>
    </html>
    '''       
    
    return html_template

def build_email_content(mail_from, mail_to, subject, content, format = 'html'):
    body = content['html']
    data = content['data']

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