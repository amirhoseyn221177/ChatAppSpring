import './action'

const intialState ={
    token:null
}


const reducer=(state=intialState,action)=>{
    if(action.type==="auth"){
       return{
           ...state,
           token:action.token
       }
    }
    return state;
}

export default reducer