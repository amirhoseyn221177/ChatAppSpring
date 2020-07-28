import React, { useState } from "react";
import Stomp from "stompjs";
import Sockjs from "sockjs-client";
var stompClient = null;

const PublicMessage = (props) => {
  const [user,setUser]=useState("")
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [grouChatName,setGroupChatName]=useState("")
  const [value,setValue]=useState("")



  var connect = (username) => {
    if (username) {
     var sockjs = new Sockjs("/ws");
      stompClient = Stomp.over(sockjs);
     stompClient.connect({}, onConnected, onError);
     stompClient.send()
      console.log(20)
    }
  };


  var onConnected = () => {
      console.log(25)
    // setChannelConnected(true);
    console.log(grouChatName)
    stompClient.subscribe(`/topic/public/${user}`,onMessageReceived)
    stompClient.send("/app/addUser", {}, JSON.stringify({ sender:user, type: 'JOIN' }))


  };

  var onError=(e)=>{
      console.error(e)
  }

  var sendMessage=()=>{
      console.log(value)
      let chatMassege={
          sender:user,
          content:value,
          type:"CHAT"
      }
      stompClient.send("/app/sendMessage",{},JSON.stringify(chatMassege))
  }


  var onMessageReceived=(payload)=>{
      console.log(51)
      console.log(payload)
      var message=JSON.parse(payload.body)
      console.log(message)
      setBroadCastMessage(prev=>[...prev,message.content])


  }

  return(
      <>
      <div>
          <h2>messages</h2>
      </div>
      <input value={user} onChange={e=>setUser(e.target.value)} placeholder="username"/>
      <form>
          <input type="text" value={value} onChange={(e)=>setValue(e.target.value)}/>
          <input type="text" value={grouChatName} onChange={e=>setGroupChatName(e.target.value)} placeholder="groupChatName"/>
          <button type="button" onClick={sendMessage}>Send</button>
          <button type="button" onClick={connect}>connect</button>
      </form>
      <div>
      {
              broadCastMessage.map(x => <p>{x}</p>)
          }
      </div>


      </>
     
  )
};

export default PublicMessage;
