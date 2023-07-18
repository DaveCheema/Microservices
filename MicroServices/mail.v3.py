"""
send report out via email
NOTE: install sendgrid client using pip command: pip install sendgrid==3.0.6
"""
import sendgrid
from sendgrid.helpers.mail import *

import sys
import base64

__SG_CLIENT = sendgrid.SendGridAPIClient(apikey='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')

__FILE_TYPES = {
    'pdf':'application/pdf',
    'png':'image/png',
    'txt':'text/plain',
    'default':'application/octet-stream'
}

def send(to = '', subject = 'sample', html = 'your report is ready', reports = None):
    ''' 
    send mail as per following conditions: to, subject, html and attachment
    NOTE: 1. attachment file path must use separator '/' instead of '\'
    '''
    status = ''
    msg = ''
    froom = 'reportingapi@ge.com'

    from_email = Email(froom)
    to_email = Email(to)
    content = Content("text/html", html)
    mail = Mail(from_email, subject, to_email, content)

    for file in reports:
        filename = file.split('/')[-1]
        fileext  = file.split('.')[-1].lower()
        filetype = __FILE_TYPES.get(fileext, __FILE_TYPES.get('default'))
        
        attachment = Attachment()

        #attachment.set_content(base64.urlsafe_b64encode(open(file, "rb").read()))
        attachment.set_content(base64.b64encode(open(file, "rb").read()))
        attachment.set_type(filetype) 
        attachment.set_filename(filename) 
        attachment.set_disposition("attachment")
        attachment.set_content_id(file)
        mail.add_attachment(attachment)
        
    
    response = __SG_CLIENT.client.mail.send.post(request_body=mail.get())
    print(response.status_code)
    print(response.body)
    print(response.headers)

    if response.status_code == 200:
        return True
    else:
        return False

def send_report(to = '', reports = None):
    '''
    send mail per given to and report file
    '''
    if len(reports) > 1:
        subject = 'your reports are ready for download'
        html = 'Here attached are reports for download.'
    else:
        subject = 'your report is ready for download'
        html = 'Here attached is the report for download.'

    send(to, subject, html, reports)

if __name__ == '__main__':
    '''
    to test the script, pls put the attachment files in the same folder as the script, e.g. 1.pdf, 2.png, test.txt
    and run the command : python mail.v3.py 1.pdf 2.png test.txt
    '''

    reports = []
    if len(sys.argv) > 1:
        reports = sys.argv[1:]
    else:
        print 'please provide at least one file as attachment'
        sys.exit(0)

    to = 'chuanyou.tang@ge.com'
    subject = ''
    if len(reports) > 1:
        subject = "reports are ready"
    else:
        subject = 'report is ready'
        
    send_report(to, reports)