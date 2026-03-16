import { saveUserInfo } from '@features/userInfoSlice.ts'
import LoginQrcode from '@imgs/login_qrcode.jpg'
import req from '@utils/request'
import { Button, Input, message } from 'antd'
import { useEffect, useState } from 'react'
import { useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import './index.less'

const loginApiName = '/user/doLogin'

const Login = () => {
  const [validCode, setValidCode] = useState('')
  const [isLoaded, setIsLoaded] = useState(false)
  const [showQrcode, setShowQrcode] = useState(false)
  const navigate = useNavigate()
  const dispatch = useDispatch()

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setIsLoaded(true)
    }, 80)
    return () => window.clearTimeout(timer)
  }, [])

  const changeCode = e => {
    setValidCode(e.target.value)
  }

  const getUserInfo = async loginId => {
    req(
      {
        method: 'post',
        url: '/user/getUserInfo',
        data: {
          userName: loginId
        }
      },
      '/auth'
    ).then(res => {
      if (res?.success && res?.data) {
        dispatch(saveUserInfo(res.data))
      }
    })
  }

  const doLogin = () => {
    if (!validCode) return
    req(
      {
        method: 'get',
        url: loginApiName,
        params: { validCode }
      },
      '/auth'
    ).then(async res => {
      if (res.success && res.data) {
        message.success('登录成功')
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        await getUserInfo(res.data.loginId)
        setTimeout(() => {
          navigate('/question-bank')
        }, 500)
      } else {
        message.error(res?.message || '登录失败，请重试')
      }
    })
  }

  const toggleQrcode = () => {
    setShowQrcode(prev => !prev)
  }

  return (
    <div className={`login-box ${isLoaded ? 'is-loaded' : ''}`}>
      <div className='login-orb login-orb-left'></div>
      <div className='login-orb login-orb-right'></div>
      <div className='login-container-inner'>
        <div className='login-story'>
          <span className='story-tag'>APE CLUB ACCESS</span>
          <h1>猿面社区</h1>
          <p className='story-desc'>
            关注公众号后发送“验证码”，输入验证码后登入。
          </p>
          <div className='story-flow'>
            <div className='story-flow-item'>
              <span>01</span>
              <p>展开入场卡，查看当前公众号二维码</p>
            </div>
            <div className='story-flow-item'>
              <span>02</span>
              <p>扫码关注后，在公众号内发送“验证码”</p>
            </div>
            <div className='story-flow-item'>
              <span>03</span>
              <p>拿到验证码后，输入直接完成登录</p>
            </div>
          </div>
        </div>
        <div className='login-console'>
          <div className='console-head'>
            <span className='console-chip'>微信验证码登录</span>
            <h2>入场卡</h2>
          </div>
          <div className={`qrcode-stage ${showQrcode ? 'is-open' : ''}`}>
            <div className='qrcode-stage-backdrop'></div>
            <button className='qrcode-trigger' type='button' onClick={toggleQrcode}>
              <span className='qrcode-trigger-radar'></span>
              <span className='qrcode-trigger-copy'>
                <strong>{showQrcode ? '收起公众号二维码' : '展开公众号二维码'}</strong>
                <em>{showQrcode ? '扫码关注后发送“验证码”' : '点击展开后再扫码进入流程'}</em>
              </span>
            </button>
            <div className='qrcode-drawer'>
              <div className='qrcode-frame'>
                <img src={LoginQrcode} alt='公众号二维码' />
              </div>
              <div className='qrcode-meta'>
                <span>扫码关注公众号</span>
                <span>回复“验证码”获取登录验证码</span>
              </div>
            </div>
          </div>
          <div className='login-form'>
            <Input
              maxLength={3}
              placeholder='输入验证码'
              onChange={changeCode}
              onPressEnter={doLogin}
              value={validCode}
            />
            <Button type='primary' onClick={doLogin}>
              进入社区
            </Button>
          </div>
          <div className='console-foot'>
            <span>验证码有效期 5 分钟</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
