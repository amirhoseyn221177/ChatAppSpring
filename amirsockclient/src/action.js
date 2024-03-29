import Axios from "axios"



export const SendingAuth = (username, password) => {
    return async dispatch => {
        try {
            let content = { password: password, username: username }
            const resp = await Axios.post('/user/login', content)
            let bearerToken = resp.headers["authorization"]
            let token = bearerToken.substring(7)
            localStorage.setItem(`token for ${username}`,token)
            dispatch(TokenAuth(token))
        } catch (e) {
            console.log(e.response)
            console.log(e.response.status)
            console.error(e.response.data)
        }

    }
}


export const TokenAuth = (token) => {
    return {
        type: "auth",
        token: token
    }

}