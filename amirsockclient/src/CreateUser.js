import React, { useState, Fragment } from "react";
import Axios from "axios";
import { withRouter } from "react-router-dom"

const CreateUser = (props) => {
  const [name, setName] = useState("");
  const [username, SetUserName] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState({})

  var sendTheUser = async () => {
    let content = {
      name: name,
      username: username,
      password: password,
      roles: [role]
    };
    try {
      const resp = await Axios.post("/user/register", content);
      console.log(resp);
    } catch (e) {
      console.error(e.message)
    }

  };

  const settingARole = (e) => {
    const name = e.target.value
    setRole({
      name: name,
      description: "regular ass user"
    })
  }


  return (
    <Fragment>
      <form>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="name"
        />
        <input
          value={username}
          onChange={(e) => SetUserName(e.target.value)}
          placeholder="username"
        />
        <input
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="password"
        />
        <input onChange={e => settingARole(e)} placeholder="Role" />
        <button type="button" onClick={sendTheUser}>
          create the user
        </button>
      </form>
    </Fragment>
  );
};


export default withRouter(CreateUser);
