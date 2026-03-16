import req from '@utils/request'
import Header from '@views/header'
import { Suspense, memo, useEffect } from 'react'
import { useDispatch } from 'react-redux'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import styles from './App.module.less'
import { saveUserInfo } from './store/features/userInfoSlice.ts'

const apiName = {
  update: '/user/update',
  queryInfo: '/user/getUserInfo'
}

const App = () => {
  const userInfoStorage = localStorage.getItem('userInfo')
  const { loginId = '' } = userInfoStorage ? JSON.parse(userInfoStorage) : {}
  const dispatch = useDispatch()

  const location = useLocation()
  const navigate = useNavigate()

  const getUserInfo = async () => {
    req(
      {
        method: 'post',
        url: apiName.queryInfo,
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

  useEffect(() => {
    if (location.pathname !== '/login' && loginId) {
      getUserInfo()
    }
  }, [])

  useEffect(() => {
    if (location.pathname === '/') {
      const userInfoStorage = localStorage.getItem('userInfo')
      if (!userInfoStorage) {
        return window.location.replace('/login')
      }
      navigate('/question-bank')
    }
  }, [location])
  const isLoginPage = location.pathname === '/login'

  return (
    <div className={styles.app}>
      <div className={styles.app__header}>
        <Header />
      </div>
      <div className={`${styles.app__content} ${isLoginPage ? styles['app__content--full'] : ''}`}>
        <Suspense fallback={<div></div>}>
          <Outlet />
        </Suspense>
      </div>
      <div className={styles.app__footer}>
        <a href='http://beian.miit.gov.cn/' target='_blank'>
          Copyright © 2026-present 猿面社区
        </a>
      </div>
    </div>
  )
}

export default memo(App)
