import React, { useState } from "react";
import Stomp from "stompjs";
import Sockjs from "sockjs-client";
import CreateGroupChat from "./CreateGroupChat";
import { withRouter } from "react-router-dom";
var stompClient = null;

const PublicMessage = (props) => {
  const [user,setUser]=useState("")
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [grouChatName,setGroupChatName]=useState("")
  const [value,setValue]=useState("")
  const [file,setFile]=useState(null)


  var connect = (username) => {
    if (username) {
     var sockjs = new Sockjs("/ws");
      stompClient = Stomp.over(sockjs);
     stompClient.connect({login:"salam ghanari"}, onConnected, onError);
      console.log(20)
    }
  };


  var onConnected = (frame) => {
      console.log(25)
      console.log(frame.headers)
    // setChannelConnected(true);
    console.log(grouChatName)
    stompClient.subscribe(`/topic/public/${grouChatName}`,onMessageReceived)
    stompClient.send("/app/addUser", {}, JSON.stringify({ sender:user, type: 'JOIN' }))


  };

  var onError=(e)=>{
      console.error(e)
  }

  var sendMessage=()=>{
      console.log(value)
      let chatMassege={
          sender:user,
          content:file,
          type:"CHAT",
          groupChats:grouChatName
      }
      stompClient.send("/app/sendMessage",{type:"image"},JSON.stringify(chatMassege))
  }


  var onMessageReceived=(payload)=>{
      console.log(51)
      console.log(payload)
      var message=JSON.parse(payload.body)
      console.log(message)
      setBroadCastMessage(prev=>[...prev,message.content])


  }

  var gettingFiles=(e)=>{
    //   console.log(64)
      let content= e.target.files
      var reader=new FileReader()
      reader.onload=()=>{
          setFile(reader.result)
          console.log(typeof(reader.result))
      }
        reader.readAsDataURL(content[0])

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
      <form>
          <input type="file" name="file" onChange={e=>gettingFiles(e)}/>
      </form>
      <div>
      {
              broadCastMessage.map(x => <p key={Math.random()*120000}>{x}</p>)
          }
          <img src={broadCastMessage[0]} alt=""/>
      </div>
      </>
     
  )
};

export default withRouter(PublicMessage);




