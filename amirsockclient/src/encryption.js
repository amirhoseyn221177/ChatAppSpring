import React, { useState } from 'react'
import Seucre from 'secure-random'
import { withRouter } from 'react-router'
import { Fragment } from 'react'
import Crypto from 'crypto-js'
import axios from 'axios'
import secureRandom from 'secure-random'
import cryptico from 'cryptico'
const Encryption = props => {
    const [AESKey, setAESKey] = useState(null)
    const [iv, setIV] = useState(null)
    const [EncryptedData, setEncryptedData] = useState(null)



    var encrypt = async () => {
        let message = "lil baby"
        let ivByte = Crypto.lib.WordArray.create(secureRandom(16, { type: "Uint8Array" }))
        var key = Crypto.lib.WordArray.create(secureRandom(32, { type: "Uint8Array" }));
        key = Crypto.enc.Utf8.parse(key)
        for (var i = key.words.length; i < 4; i++) {
            key.words[i] = 0;
        }

        console.log(key.words)


        key.sigBytes = 32;
        key.clamp();

        const AES = Crypto.AES.encrypt(message, key, {
            iv: ivByte,
            mode: Crypto.mode.CBC,
            padding: Crypto.pad.Pkcs7,
        })





        // const decrypt = Crypto.AES.decrypt(AES.toString(), key, {
        //     iv: ivByte,
        //     mode: Crypto.mode.CBC,
        //     padding: Crypto.pad.Pkcs7
        // })

        // await axios.post("http://localhost:8080/user/dec", {
        //     key: key.toString(Crypto.enc.Base64),
        //     iv: ivByte.toString(Crypto.enc.Base64),
        //     text: AES.ciphertext.toString(Crypto.enc.Base64)
        // })


    }


    var decryption = (encryptedData, key, AESIv) => {
        const decrypt = Crypto.AES.decrypt(encryptedData.toString(), key, {
            iv: AESIv,
            mode: Crypto.mode.CBC,
            padding: Crypto.pad.Pkcs7
        })

        console.log(decrypt.toString(Crypto.enc.Utf8))
        return decrypt;
    }


    var creatingRSAKeyPair=()=>{
        var passphrase ="polo"

        var KeyPair = cryptico.generateRSAKey(passphrase,1024)
        console.log(cryptico.publicKeyString(KeyPair))
    }






    return (
        <Fragment>
            <button onClick={creatingRSAKeyPair}>encryption</button>
        </Fragment>
    )


}


export default withRouter(Encryption);