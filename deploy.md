## Deploy Locally

     lein uberjar
     lein install

## Deploy to Clojars

### Setting up GPG

Need to have GPG installed from  https://gpgtools.org/installer/index.html

Keys can be viewed:

       gpg --list-keys

Public key can be shared:

     gpg --send-keys B826A1BC249B92FB

Key is specified in user profile:

     :signing {:gpg-key "B826A1BC249B92FB"}


### To get a deploy token

1. Login to Clojars
2. Need a 2 factor token from Digidentity on my iPhone
3. Go to https://clojars.org/tokens
4. Copy token starting CLOJARS_*

### To actually deploy

To deploy do

     lein deploy clojars

Then enter username "rachbowyer" and for the password the deploy token
Will then sign using GPG. If the passphrase is not in the computer's memory,
it will ask you for the passphrase

## After the release

Need to bump the version number of the project to 

    henderson "0.1.x"

